package com.projecte.expansion.tile;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
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
import moze_intel.projecte.api.tile.IEmcProvider;
import moze_intel.projecte.utils.WorldHelper;

/**
 * 反物质继电器(Relay)的方块实体:传输 EMC。
 *
 * <p>
 * 存量(缓冲)用 BigInteger,上限与上游一致 = {@code Long.MAX_VALUE}。每 20 tick(固定,不跟随
 * {@link Config#tickDelay},免得改 tickDelay 时放大传输量)把 {@code min(存量, 每秒传输上限)}
 * 均分给四周**非继电器**的 EMC 接收方:优先走本 mod 的 BigInteger 接口
 * ({@link IPXEmcAcceptorBig}),对方只有 PE 的 double 接口时回退到
 * {@link IEmcAcceptor#acceptEMC(ForgeDirection, double)}。
 *
 * <p>
 * 继电器之间不互相传(既不发也不收),避免来回抖动;而"把 EMC 注入玩家/团队 EMC 系统"由紧贴的
 * EMC 接口负责,继电器自己永远不碰 {@code PXEmcAccess}。
 *
 * <p>
 * 加成:{@link Matter#relayBonus} 是"每个相邻收集器每秒给自己多少 EMC",由收集器每次产出时调用
 * {@link #addRelayBonus(int)} 送进来,和 ProjectE 里收集器给中继器发加成一致。
 */
public class TileEntityRelayPX extends TileEntity implements IEmcAcceptor, IEmcProvider, IPXEmcAcceptorBig, IPXRelay {

    /** 固定 20 tick 传一次。 */
    private static final int TRANSFER_INTERVAL = 20;

    private static final String NBT_STORED = "StoredEmc";
    private static final String NBT_PENDING_BONUS = "PendingBonus";

    private final Matter matter;
    private UUID ownerUUID;
    private String ownerName = "";
    private BigInteger stored = BigInteger.ZERO;
    /** 不足 1 EMC 的加成余数(加成倍率不是整数时才用得上)。 */
    private BigInteger pendingBonus = BigInteger.ZERO;
    private int tickCounter;

    public TileEntityRelayPX(Matter matter) {
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

    public BigInteger getStoredEmcBig() {
        return stored;
    }

    /** @return 存量上限,与上游一致:Long.MAX_VALUE。 */
    public BigInteger getMaxEmcBig() {
        return BigInteger.valueOf(Long.MAX_VALUE);
    }

    /** @return 每秒的继电器加成(已乘配置倍率)。 */
    public BigInteger getPerSecondBonus() {
        return BigDecimal.valueOf(matter.relayBonus)
            .multiply(BigDecimal.valueOf(Config.relayBonusMultiplier))
            .toBigInteger();
    }

    /** @return 每秒能传出去的 EMC 上限(已乘配置倍率)。 */
    public BigInteger getPerSecondTransfer() {
        return BigDecimal.valueOf(matter.relayTransfer)
            .multiply(BigDecimal.valueOf(Config.relayTransferMultiplier))
            .toBigInteger();
    }

    // 传输

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        if (worldObj == null || worldObj.isRemote) {
            return;
        }
        if (++tickCounter < TRANSFER_INTERVAL) {
            return;
        }
        tickCounter = 0;
        transfer();
    }

    /** 把 min(存量, 每秒上限)均分给四周非继电器的接收方;没人接就继续留在自己这儿。 */
    private void transfer() {
        if (stored.signum() <= 0) {
            return;
        }
        BigInteger amount = stored.min(getPerSecondTransfer());
        if (amount.signum() <= 0) {
            return;
        }

        List<Map.Entry<ForgeDirection, TileEntity>> targets = receivers();
        if (targets.isEmpty()) {
            return;
        }
        BigInteger share = amount.divide(BigInteger.valueOf(targets.size()));
        if (share.signum() <= 0) {
            return;
        }

        for (Map.Entry<ForgeDirection, TileEntity> entry : targets) {
            BigInteger accepted = give(entry.getValue(), entry.getKey(), share);
            stored = stored.subtract(accepted);
            if (stored.signum() <= 0) {
                stored = BigInteger.ZERO;
                break;
            }
        }
        markDirty();
    }

    /** 优先用本 mod 的精确接口,对方只有 PE 的 double 接口时回退(>2^53 会有个位误差)。 */
    private static BigInteger give(TileEntity target, ForgeDirection side, BigInteger amount) {
        if (target instanceof IPXEmcAcceptorBig) {
            BigInteger accepted = ((IPXEmcAcceptorBig) target).acceptEmcBig(side, amount);
            return accepted == null || accepted.signum() <= 0 ? BigInteger.ZERO : accepted;
        }
        double accepted = ((IEmcAcceptor) target).acceptEMC(side, amount.doubleValue());
        return accepted <= 0D ? BigInteger.ZERO
            : BigDecimal.valueOf(accepted)
                .toBigInteger();
    }

    /** @return 四周能收 EMC 且不是继电器的方块。 */
    private List<Map.Entry<ForgeDirection, TileEntity>> receivers() {
        List<Map.Entry<ForgeDirection, TileEntity>> out = new ArrayList<>();
        Map<ForgeDirection, TileEntity> adjacent = WorldHelper.getAdjacentTileEntitiesMapped(worldObj, this);
        if (adjacent == null) {
            return out;
        }
        for (Map.Entry<ForgeDirection, TileEntity> entry : adjacent.entrySet()) {
            TileEntity tile = entry.getValue();
            if (tile instanceof IPXRelay) {
                continue; // 继电器之间不互传,避免抖动
            }
            if (tile instanceof IPXEmcAcceptorBig || tile instanceof IEmcAcceptor) {
                out.add(entry);
            }
        }
        return out;
    }

    // 接收(收集器 / PE 中继器推过来,也允许别人来拉)

    @Override
    public BigInteger acceptEmcBig(ForgeDirection side, BigInteger toAccept) {
        if (toAccept == null || toAccept.signum() <= 0 || isRelayNeighbour(side)) {
            return BigInteger.ZERO;
        }
        BigInteger space = getMaxEmcBig().subtract(stored);
        if (space.signum() <= 0) {
            return BigInteger.ZERO;
        }
        BigInteger accepted = space.min(toAccept);
        stored = stored.add(accepted);
        markDirty();
        return accepted;
    }

    @Override
    public double acceptEMC(ForgeDirection side, double toAccept) {
        if (toAccept <= 0D) {
            return 0D;
        }
        return acceptEmcBig(
            side,
            BigDecimal.valueOf(toAccept)
                .toBigInteger()).doubleValue();
    }

    @Override
    public double provideEMC(ForgeDirection side, double toExtract) {
        if (toExtract <= 0D || stored.signum() <= 0) {
            return 0D;
        }
        BigInteger given = stored.min(
            BigDecimal.valueOf(toExtract)
                .toBigInteger());
        stored = stored.subtract(given);
        markDirty();
        return given.doubleValue();
    }

    @Override
    public double getStoredEmc() {
        return stored.doubleValue();
    }

    @Override
    public double getMaximumEmc() {
        return getMaxEmcBig().doubleValue();
    }

    /**
     * PE 的 {@code acceptEMC} 传进来的 side 是"发送方 -> 本方块",所以发送方在本方块的相反方向。
     * 识别不出来时按"不是继电器"处理(宁可收下也不静默丢 EMC)。
     */
    private boolean isRelayNeighbour(ForgeDirection fromSender) {
        if (fromSender == null || fromSender == ForgeDirection.UNKNOWN) {
            return false;
        }
        ForgeDirection senderSide = fromSender.getOpposite();
        TileEntity tile = worldObj
            .getTileEntity(xCoord + senderSide.offsetX, yCoord + senderSide.offsetY, zCoord + senderSide.offsetZ);
        return tile instanceof IPXRelay;
    }

    // 加成(由相邻收集器驱动)

    @Override
    public void addRelayBonus(int ticks) {
        if (worldObj == null || worldObj.isRemote || ticks <= 0) {
            return;
        }
        BigInteger bonus = BigDecimal.valueOf(matter.relayBonus)
            .multiply(BigDecimal.valueOf(Config.relayBonusMultiplier))
            .multiply(BigDecimal.valueOf(ticks))
            .divide(BigDecimal.valueOf(20), 3, RoundingMode.UP)
            .toBigInteger();
        pendingBonus = pendingBonus.add(bonus);
        if (pendingBonus.signum() <= 0) {
            return;
        }
        BigInteger space = getMaxEmcBig().subtract(stored);
        if (space.signum() > 0) {
            stored = stored.add(pendingBonus.min(space));
        }
        pendingBonus = BigInteger.ZERO;
        markDirty();
    }

    // 生命周期

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        ownerName = compound.getString("OwnerName");
        if (compound.hasKey("Owner")) {
            try {
                ownerUUID = UUID.fromString(compound.getString("Owner"));
            } catch (IllegalArgumentException ignored) {
                ownerUUID = null;
            }
        }
        stored = readBig(compound, NBT_STORED);
        pendingBonus = readBig(compound, NBT_PENDING_BONUS);
        tickCounter = 0;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setString("OwnerName", ownerName == null ? "" : ownerName);
        if (ownerUUID != null) {
            compound.setString("Owner", ownerUUID.toString());
        }
        if (stored.signum() != 0) {
            compound.setString(NBT_STORED, stored.toString());
        }
        if (pendingBonus.signum() != 0) {
            compound.setString(NBT_PENDING_BONUS, pendingBonus.toString());
        }
    }

    private static BigInteger readBig(NBTTagCompound compound, String key) {
        if (!compound.hasKey(key)) {
            return BigInteger.ZERO;
        }
        try {
            return new BigInteger(compound.getString(key));
        } catch (NumberFormatException ignored) {
            return BigInteger.ZERO;
        }
    }
}
