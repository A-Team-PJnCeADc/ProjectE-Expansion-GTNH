package com.projecte.expansion.tile;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import com.projecte.expansion.Config;
import com.projecte.expansion.api.IPXEmcAcceptorBig;
import com.projecte.expansion.api.IPXRelay;
import com.projecte.expansion.util.Matter;

import moze_intel.projecte.api.tile.IEmcAcceptor;
import moze_intel.projecte.utils.WorldHelper;

/**
 * 能量收集器(Collector)的方块实体:只产生 EMC。
 *
 * <p>
 * 速率见 {@link Matter#collectorOutput}(EMC/秒,各层级数值见表,再乘
 * {@link Config#collectorMultiplier});每 {@link Config#tickDelay} tick(默认 20 = 1 秒)
 * 产生一次,并**立刻**均分发给四周能接收 EMC 的方块(即 PE 的 {@link IEmcAcceptor}:
 * 本 mod 的继电器/EMC 接口,以及 ProjectE 自己的中继器、凝聚器等)。
 *
 * <p>
 * 和上游那台带缓冲、还会充电/升级燃料的收集器不同:
 * <ul>
 * <li>**不缓存**:产生的 EMC 当次就交给接收方;没人接(或接收方只收得下一部分)就直接消失,
 * 方块自己不保存任何 EMC。</li>
 * <li>**不进个人/团队 EMC 系统**:收集器不碰 {@code PXEmcAccess},也就不会直接把 EMC 记到玩家或团队池;
 * 那是由下游的继电器 / EMC 接口决定的。</li>
 * </ul>
 *
 * <p>
 * 所有者(放置它的人)只用于显示:右击与 Waila 会报出所有者、每秒产出。收集器本身不会因为所有者是谁
 * 而改变产量或去向。
 */
public class TileEntityCollectorPX extends TileEntity {

    private static final String NBT_OWNER = "Owner";
    private static final String NBT_OWNER_NAME = "OwnerName";

    private final Matter matter;
    private UUID ownerUUID;
    private String ownerName = "";
    private int tickCounter;

    public TileEntityCollectorPX(Matter matter) {
        this.matter = matter;
    }

    public Matter getMatter() {
        return matter;
    }

    // 所有者(仅显示用)

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

    /** @return 该收集器每秒产生的 EMC(已乘配置倍率),供显示用。 */
    public BigInteger getPerSecondOutput() {
        return BigDecimal.valueOf(matter.collectorOutput)
            .multiply(BigDecimal.valueOf(Config.collectorMultiplier))
            .toBigInteger();
    }

    /** @return 本次结算要发出的 EMC(tickDelay 不是 20 时按比例折算,可带小数)。 */
    public double getPerOperationOutput() {
        return matter.collectorOutput * Config.collectorMultiplier * Math.max(1, Config.tickDelay) / 20D;
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        if (worldObj == null || worldObj.isRemote) {
            return;
        }
        if (++tickCounter < Math.max(1, Config.tickDelay)) {
            return;
        }
        tickCounter = 0;
        pushToAcceptors();
        sendRelayBonus();
    }

    /**
     * 把这一次产生的 EMC 均分给四周的接收方;余量不回收(收集器不缓存)。
     */
    private void pushToAcceptors() {
        BigInteger amount = getPerOperationOutputBig();
        if (amount.signum() <= 0) {
            return;
        }

        Map<ForgeDirection, TileEntity> adjacent = WorldHelper.getAdjacentTileEntitiesMapped(worldObj, this);
        if (adjacent == null || adjacent.isEmpty()) {
            return;
        }

        int receivers = 0;
        for (TileEntity tile : adjacent.values()) {
            if (isReceiver(tile)) {
                receivers++;
            }
        }
        if (receivers == 0) {
            return;
        }

        BigInteger share = amount.divide(BigInteger.valueOf(receivers));
        if (share.signum() <= 0) {
            return;
        }

        for (Map.Entry<ForgeDirection, TileEntity> entry : adjacent.entrySet()) {
            TileEntity tile = entry.getValue();
            if (!isReceiver(tile)) {
                continue;
            }
            if (tile instanceof IPXEmcAcceptorBig) {
                // 本 mod 的机器之间走精确接口
                ((IPXEmcAcceptorBig) tile).acceptEmcBig(entry.getKey(), share);
            } else {
                ((IEmcAcceptor) tile).acceptEMC(entry.getKey(), share.doubleValue());
            }
            // 收下多少都不回收:收集器不缓存
        }
    }

    private static boolean isReceiver(TileEntity tile) {
        return tile instanceof IEmcAcceptor || tile instanceof IPXEmcAcceptorBig;
    }

    /** @return 本次结算要发出的 EMC(tickDelay 不是 20 时按比例折算)。 */
    private BigInteger getPerOperationOutputBig() {
        return BigDecimal.valueOf(matter.collectorOutput)
            .multiply(BigDecimal.valueOf(Config.collectorMultiplier))
            .multiply(BigDecimal.valueOf(Math.max(1, Config.tickDelay)))
            .divide(BigDecimal.valueOf(20), 0, RoundingMode.DOWN)
            .toBigInteger();
    }

    /** 通知四周的继电器发放这一次的继电器加成(和 ProjectE 里收集器给中继器发加成同源)。 */
    private void sendRelayBonus() {
        Map<ForgeDirection, TileEntity> adjacent = WorldHelper.getAdjacentTileEntitiesMapped(worldObj, this);
        if (adjacent == null || adjacent.isEmpty()) {
            return;
        }
        int ticks = Math.max(1, Config.tickDelay);
        for (TileEntity tile : adjacent.values()) {
            if (tile instanceof IPXRelay) {
                ((IPXRelay) tile).addRelayBonus(ticks);
            }
        }
    }
}
