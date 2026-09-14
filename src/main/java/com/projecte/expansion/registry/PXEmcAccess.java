package com.projecte.expansion.registry;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.projecte.expansion.api.IPXEmcAccess;

import moze_intel.projecte.playerData.Transmutation;

/**
 * 所有者 EMC / 知识访问入口(门面)。
 *
 * <p>
 * 选择顺序:<b>其它 mod 注册的实现</b> -> 装了 ProjectE-Team 时的反射实现
 * ({@link ProjectETeamAccess}) -> 纯 ProjectE 实现({@link ProjectEAccess})。
 * 本 mod 的机器一律通过这里读写玩家的 EMC 与知识,因此团队模式无需机器侧特判。
 */
public final class PXEmcAccess {

    private static IPXEmcAccess custom;
    private static final IPXEmcAccess PROJECT_E = new ProjectEAccess();
    private static IPXEmcAccess team;

    private PXEmcAccess() {}

    /** 注册自定义实现(会覆盖默认与团队实现)。 */
    public static void register(IPXEmcAccess access) {
        custom = access;
    }

    public static IPXEmcAccess get() {
        if (custom != null) {
            return custom;
        }
        IPXEmcAccess teamAccess = team;
        if (teamAccess == null && ProjectETeamAccess.isAvailable()) {
            teamAccess = team = new ProjectETeamAccess();
        }
        return teamAccess != null ? teamAccess : PROJECT_E;
    }

    public static double getEmc(EntityPlayer owner) {
        return get().getEmc(owner);
    }

    public static void addEmc(EntityPlayer owner, double amount) {
        get().addEmc(owner, amount);
    }

    public static List<ItemStack> getKnowledge(EntityPlayer owner) {
        return get().getKnowledge(owner);
    }

    /** @return 该所有者当前是否在使用团队(共享)数据。 */
    public static boolean isShared(EntityPlayer owner) {
        return get().isShared(owner);
    }

    /** @return 所有者当前 EMC 的精确值(BigInteger)。 */
    public static java.math.BigInteger getEmcBig(EntityPlayer owner) {
        return get().getEmcBig(owner);
    }

    /** 按精确值增加/扣除(delta 为负表示扣除);实现不支持时自动回退到 double 路径。 */
    public static void addEmcBig(EntityPlayer owner, java.math.BigInteger delta) {
        if (delta == null || delta.signum() == 0) {
            return;
        }
        if (!get().addEmcBig(owner, delta)) {
            get().addEmc(owner, PXEmcTable.toPeDouble(delta));
        }
    }

    /** 纯 ProjectE 实现(项目自带的玩家 EMC 是 double,存档与网络包都是 double)。 */
    public static final class ProjectEAccess implements IPXEmcAccess {

        @Override
        public double getEmc(EntityPlayer owner) {
            return Transmutation.getEmc(owner);
        }

        @Override
        public void addEmc(EntityPlayer owner, double amount) {
            if (amount == 0) {
                return;
            }
            double result = Math.max(0D, Math.min(Transmutation.getEmc(owner) + amount, PXEmcTable.PE_EMC_MAX));
            Transmutation.setEmc(owner, result);
        }

        @Override
        public List<ItemStack> getKnowledge(EntityPlayer owner) {
            return Transmutation.getKnowledge(owner);
        }

        @Override
        public boolean isShared(EntityPlayer owner) {
            return false;
        }
    }
}
