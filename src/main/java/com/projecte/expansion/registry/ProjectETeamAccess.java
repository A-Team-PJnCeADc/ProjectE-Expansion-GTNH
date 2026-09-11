package com.projecte.expansion.registry;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.projecte.expansion.api.IPXEmcAccess;

import cpw.mods.fml.common.Loader;
import moze_intel.projecte.playerData.Transmutation;

/**
 * ProjectE-Team(projecteteam)的反射实现:团队开启 EMC / 知识共享时走团队池。
 *
 * <p>
 * 走反射而不是编译依赖,保证本 mod 在没有 ProjectE-Team 时也能编译与运行。
 * 被调用的都是 {@code com.projecte.team.TeamManager} 的 public static 方法:
 * {@code isSharingEMC/isSharingKnowledge/getTeamEmc/onEmcAdd/onEmcRemove/getTeamKnowledge}。
 *
 * <p>
 * 注意 EMC 必须用**增量**接口({@code onEmcAdd}/{@code onEmcRemove}):团队共享时
 * ProjectE 的绝对写入会被 ProjectE-Team 忽略(它以团队池为权威值)。
 */
public final class ProjectETeamAccess implements IPXEmcAccess {

    private static final String TEAM_MANAGER = "com.projecte.team.TeamManager";

    private static Method getTeamMethod;
    private static Method teamIsShareEmc;
    private static Method teamGetEmc;
    private static Method teamSetEmc;
    private static Method markDirtyMethod;
    private static Method getTeamEmcBigMethod;
    private static Method addTeamEmcBigMethod;
    private static Method syncTeamEmcMethod;
    private static Method isSharingEmc;
    private static Method isSharingKnowledge;
    private static Method getTeamEmc;
    private static Method onEmcAdd;
    private static Method onEmcRemove;
    private static Method getTeamKnowledge;
    private static boolean resolved;
    private static boolean available;

    /** @return 是否装载了 ProjectE-Team 且其 TeamManager API 与预期一致。 */
    public static boolean isAvailable() {
        if (!resolved) {
            resolved = true;
            available = Loader.isModLoaded("projecteteam") && resolve();
        }
        return available;
    }

    private static boolean resolve() {
        try {
            Class<?> manager = Class.forName(TEAM_MANAGER);
            isSharingEmc = manager.getMethod("isSharingEMC", EntityPlayer.class);
            isSharingKnowledge = manager.getMethod("isSharingKnowledge", EntityPlayer.class);
            getTeamEmc = manager.getMethod("getTeamEmc", EntityPlayer.class);
            onEmcAdd = manager.getMethod("onEmcAdd", EntityPlayer.class, double.class);
            onEmcRemove = manager.getMethod("onEmcRemove", EntityPlayer.class, double.class);
            getTeamKnowledge = manager.getMethod("getTeamKnowledge", EntityPlayer.class);

            // 直接写团队池所需的成员(绕开 ProjectE-Team 的 TILE_MAX_EMC 夹取)
            getTeamMethod = manager.getMethod("getTeam", EntityPlayer.class);
            Class<?> teamData = Class.forName("com.projecte.team.TeamData");
            teamIsShareEmc = teamData.getMethod("isShareEMC");
            teamGetEmc = teamData.getMethod("getEmc");
            teamSetEmc = teamData.getMethod("setEmc", double.class);
            syncTeamEmcMethod = manager.getMethod("syncTeamEmc", teamData);
            try {
                // 新一点版本的 ProjectE-Team:BigInteger 团队池(装了本 mod 时不限制上限)
                getTeamEmcBigMethod = manager.getMethod("getTeamEmcBig", EntityPlayer.class);
                addTeamEmcBigMethod = manager
                    .getMethod("addTeamEmcBig", EntityPlayer.class, java.math.BigInteger.class);
            } catch (NoSuchMethodException olderVersion) {
                getTeamEmcBigMethod = null;
                addTeamEmcBigMethod = null;
            }
            markDirtyMethod = manager.getDeclaredMethod("markDirty");
            markDirtyMethod.setAccessible(true);
            return true;
        } catch (Throwable t) {
            PXPatchLog.mark("ProjectE-Team 反射接入失败(将回退到 ProjectE 个人数据): " + t);
            return false;
        }
    }

    private static boolean sharingEmc(EntityPlayer owner) {
        try {
            return Boolean.TRUE.equals(isSharingEmc.invoke(null, owner));
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean sharingKnowledge(EntityPlayer owner) {
        try {
            return Boolean.TRUE.equals(isSharingKnowledge.invoke(null, owner));
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public double getEmc(EntityPlayer owner) {
        if (sharingEmc(owner)) {
            try {
                return ((Number) getTeamEmc.invoke(null, owner)).doubleValue();
            } catch (Throwable t) {
                // 回退到 ProjectE 个人值
            }
        }
        return Transmutation.getEmc(owner);
    }

    /**
     * 把 EMC 增量**直接**写进团队池。
     *
     * <p>
     * 不走 ProjectE-Team 的 {@code onEmcAdd/onEmcRemove}:那两个方法把团队池夹在
     * {@code Constants.TILE_MAX_EMC}(2^30)上,高价物品一卖就会把团队 EMC "写成上限值"
     * (表现为"卖东西直接改了 EMC 上限/总数值")。这里用他们的 {@code TeamData.setEmc}
     * (本身不夹取)加本 mod 的上限 {@link PXEmcTable#PE_EMC_MAX},再照常 markDirty + 同步全队。
     *
     * @return true 表示已由团队池处理,调用方不必再回退到 ProjectE 个人数据
     */
    public static boolean applyTeamEmcDelta(EntityPlayer owner, double amount) {
        if (amount == 0 || !isAvailable()) {
            return false;
        }
        try {
            Object team = getTeamMethod.invoke(null, owner);
            if (team == null || !Boolean.TRUE.equals(teamIsShareEmc.invoke(team))) {
                return false;
            }
            double current = ((Number) teamGetEmc.invoke(team)).doubleValue();
            double updated = Math.max(0D, Math.min(current + amount, PXEmcTable.PE_EMC_MAX));
            teamSetEmc.invoke(team, updated);
            markDirtyMethod.invoke(null);
            syncTeamEmcMethod.invoke(null, team);
            return true;
        } catch (Throwable t) {
            PXPatchLog.mark("团队 EMC 直接写入失败(回退 ProjectE-Team 原逻辑): " + t);
            return false;
        }
    }

    @Override
    public void addEmc(EntityPlayer owner, double amount) {
        if (amount == 0) {
            return;
        }
        if (applyTeamEmcDelta(owner, amount)) {
            return;
        }
        if (sharingEmc(owner)) {
            try {
                if (amount > 0) {
                    onEmcAdd.invoke(null, owner, amount);
                } else {
                    onEmcRemove.invoke(null, owner, -amount);
                }
                return;
            } catch (Throwable t) {
                // 回退到 ProjectE 个人值
            }
        }
        double result = Math.max(0D, Math.min(Transmutation.getEmc(owner) + amount, PXEmcTable.PE_EMC_MAX));
        Transmutation.setEmc(owner, result);
    }

    @Override
    public java.math.BigInteger getEmcBig(EntityPlayer owner) {
        if (sharingEmc(owner) && getTeamEmcBigMethod != null) {
            try {
                return (java.math.BigInteger) getTeamEmcBigMethod.invoke(null, owner);
            } catch (Throwable t) {
                // 回退到 double 视图
            }
        }
        return PXEmcTable.fromPe(getEmc(owner));
    }

    @Override
    public boolean addEmcBig(EntityPlayer owner, java.math.BigInteger delta) {
        if (delta == null || delta.signum() == 0) {
            return true;
        }
        if (addTeamEmcBigMethod != null) {
            try {
                if (Boolean.TRUE.equals(addTeamEmcBigMethod.invoke(null, owner, delta))) {
                    return true;
                }
            } catch (Throwable t) {
                PXPatchLog.mark("团队 BigInteger 记账失败(回退 double): " + t);
            }
        }
        return false;
    }

    @Override
    public boolean isShared(EntityPlayer owner) {
        return sharingEmc(owner) || sharingKnowledge(owner);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ItemStack> getKnowledge(EntityPlayer owner) {
        if (sharingKnowledge(owner)) {
            try {
                List<ItemStack> knowledge = (List<ItemStack>) getTeamKnowledge.invoke(null, owner);
                if (knowledge != null) {
                    return knowledge;
                }
            } catch (Throwable t) {
                // 回退到 ProjectE 个人值
            }
        }
        List<ItemStack> knowledge = Transmutation.getKnowledge(owner);
        return knowledge != null ? knowledge : Collections.<ItemStack>emptyList();
    }
}
