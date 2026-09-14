package com.projecte.expansion.tile;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;

import com.projecte.expansion.Config;
import com.projecte.expansion.registry.PXEmcAccess;
import com.projecte.expansion.util.Matter;

/**
 * 力量花(Power Flower)的方块实体:每 20 游戏刻把 EMC 直接打进所有者的 EMC 系统。
 *
 * <p>
 * 产量见表 {@link Matter#powerFlowerOutput}(就是"18 个同级收集器 + 30 个同级继电器"的量,再乘
 * {@link Config#powerflowerMultiplier}),走 {@link PXEmcAccess},因此所有者所在团队开启共享时进的是
 * 团队池(装了 ProjectE-Team 时是 BigInteger 团队池)。
 *
 * <p>
 * 它不像收集器那样往四周推 EMC:力量花自带整套收集器/继电器结构,产量直接归所有者,不需要离地。
 * 所有者离线时不产(没有缓冲),也不会把 EMC 记给别人。
 */
public class TileEntityPowerFlowerPX extends TileEntity {

    /** 固定 20 tick 产一次,不跟随 tickDelay。 */
    private static final int INTERVAL = 20;

    private static final String NBT_OWNER = "Owner";
    private static final String NBT_OWNER_NAME = "OwnerName";

    private final Matter matter;
    private UUID ownerUUID;
    private String ownerName = "";
    private int tickCounter;

    public TileEntityPowerFlowerPX(Matter matter) {
        this.matter = matter;
    }

    public Matter getMatter() {
        return matter;
    }

    /** @return 每 20 tick 产生的 EMC(已乘配置倍率),供显示用。 */
    public BigInteger getPerSecondOutput() {
        return BigDecimal.valueOf(matter.powerFlowerOutput)
            .multiply(BigDecimal.valueOf(Config.powerflowerMultiplier))
            .toBigInteger();
    }

    // 所有者

    public boolean hasOwner() {
        return ownerUUID != null;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public String getOwnerName() {
        if (ownerName != null && !ownerName.isEmpty()) {
            return ownerName;
        }
        return ownerUUID == null ? ""
            : ownerUUID.toString()
                .substring(0, 8);
    }

    public void setOwner(EntityPlayer player) {
        if (player == null) {
            return;
        }
        ownerUUID = player.getUniqueID();
        ownerName = player.getCommandSenderName();
        markDirty();
    }

    public boolean isOwner(EntityPlayer player) {
        return player != null && ownerUUID != null && ownerUUID.equals(player.getUniqueID());
    }

    /** @return 在线所有者(离线返回 null:此时不产 EMC)。 */
    public EntityPlayer getOwnerPlayer() {
        if (ownerUUID == null || worldObj == null || worldObj.isRemote) {
            return null;
        }
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null) {
            return null;
        }
        List<?> players = server.getConfigurationManager().playerEntityList;
        if (players == null) {
            return null;
        }
        for (Object candidate : players) {
            if (candidate instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) candidate;
                if (ownerUUID.equals(player.getUniqueID())) {
                    return player;
                }
            }
        }
        return null;
    }

    // 产出

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        if (worldObj == null || worldObj.isRemote) {
            return;
        }
        if (++tickCounter < INTERVAL) {
            return;
        }
        tickCounter = 0;

        EntityPlayer owner = getOwnerPlayer();
        if (owner == null) {
            return; // 所有者不在线:不产
        }
        BigInteger amount = getPerSecondOutput();
        if (amount.signum() <= 0) {
            return;
        }
        PXEmcAccess.addEmcBig(owner, amount);
    }

    // 生命周期

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        ownerName = compound.getString(NBT_OWNER_NAME);
        if (compound.hasKey(NBT_OWNER)) {
            try {
                ownerUUID = UUID.fromString(compound.getString(NBT_OWNER));
            } catch (IllegalArgumentException ignored) {
                ownerUUID = null;
            }
        }
        tickCounter = 0;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setString(NBT_OWNER_NAME, ownerName == null ? "" : ownerName);
        if (ownerUUID != null) {
            compound.setString(NBT_OWNER, ownerUUID.toString());
        }
    }
}
