package com.projecte.expansion.tile;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.projecte.expansion.api.IPXEmcAcceptorBig;
import com.projecte.expansion.registry.PXEmcAccess;
import com.projecte.expansion.registry.PXEmcTable;
import com.projecte.expansion.util.Matter;

import moze_intel.projecte.api.tile.IEmcAcceptor;
import moze_intel.projecte.playerData.Transmutation;

/**
 * EMC 接口(EMC Link)的方块实体:把 EMC 注入所有者的 EMC 系统,并让所有者用 EMC 换物品/流体。
 *
 * <p>
 * 三件事:
 * <ol>
 * <li>**注入 EMC**:相邻继电器/收集器推来的 EMC(以及管道送进来的物品所值的 EMC)都记给所有者;
 * 所有者所在团队开启共享时,{@link PXEmcAccess} 会把它记进团队池。每秒最多注入
 * {@link Matter#getEmcLinkEmcLimit()}(MK16 无上限)。</li>
 * <li>**导入物品**:槽位 1 起接收物品(按精确价 {@link PXEmcTable} 折算成 EMC 并学习该物品),
 * 每秒最多 {@link Matter#getEmcLinkItemLimit()} 个。所有者离线时物品留在槽里不吞。</li>
 * <li>**导出物品/流体**:槽位 0 是绑定物品(虚拟槽,报出所有者当前买得起的数量),管道抽走时按精确价扣
 * 所有者 EMC,每秒最多 {@link Matter#getEmcLinkItemLimit()} 个;绑定的若是装满的桶,则实现
 * {@link IFluidHandler},管道可按 mB 抽走流体(每 mB 价 = 整桶价扣掉桶本身价值后的均价),每秒最多
 * {@link Matter#getEmcLinkFluidLimit()} mB。</li>
 * </ol>
 *
 * <p>
 * NBT 过滤:元数据 0(默认)无视带 NBT/已消耗耐久的物品(既不收也不导),元数据 1(贤者之石潜行右击切换,
 * 贴图变黄)忽略一切 NBT。每秒额度固定 20 tick 结算一次,不跟随 {@link Config#tickDelay}。
 */
public class TileEntityEMCLinkPX extends TileEntity
    implements IInventory, ISidedInventory, IFluidHandler, IEmcAcceptor, IPXEmcAcceptorBig {

    /** 槽位 0 = 导出槽(绑定物品的虚拟槽)。 */
    public static final int EXPORT_SLOT = 0;
    /** 首个导入槽位。 */
    public static final int IMPORT_SLOT = 1;

    private static final int TICK_INTERVAL = 20;
    private static final int BUCKET_VOLUME = FluidContainerRegistry.BUCKET_VOLUME;

    private static final String NBT_OWNER = "Owner";
    private static final String NBT_OWNER_NAME = "OwnerName";
    private static final String NBT_BOUND = "Bound";
    private static final String NBT_BUFFERED_EMC = "BufferedEmc";
    private static final String NBT_PENDING = "Pending";

    private final Matter matter;
    private UUID ownerUUID;
    private String ownerName = "";
    /** 绑定的导出物品(流体时是装满的桶)。 */
    private ItemStack bound;
    /** 已收到但还没注入所有者的 EMC(所有者离线时先攒着)。 */
    private BigInteger bufferedEmc = BigInteger.ZERO;
    /** 导入槽暂存的物品:下一 tick 转成 EMC,避免所有者离线时吞物品。 */
    private ItemStack pending;
    /** 每秒额度:EMC(null = 无限)、导入个数、导出个数、流体(mB)。 */
    private BigInteger remainingEmc = BigInteger.ZERO;
    private int remainingImport;
    private int remainingExport;
    private int remainingFluid = 0;
    private int tickCounter;

    public TileEntityEMCLinkPX(Matter matter) {
        this.matter = matter;
        resetLimits();
    }

    public Matter getMatter() {
        return matter;
    }

    @Override
    public void validate() {
        super.validate();
        resetLimits();
    }

    private void resetLimits() {
        remainingEmc = matter.getEmcLinkEmcLimit();
        remainingImport = matter.getEmcLinkItemLimit();
        remainingExport = matter.getEmcLinkItemLimit();
        remainingFluid = matter.getEmcLinkFluidLimit();
    }

    /** @return 每秒能注入所有者的 EMC 上限(MK16 为 null = 无上限)。 */
    public BigInteger getPerSecondEmcLimit() {
        return matter.getEmcLinkEmcLimit();
    }

    public int getPerSecondItemLimit() {
        return matter.getEmcLinkItemLimit();
    }

    public int getPerSecondFluidLimit() {
        return matter.getEmcLinkFluidLimit();
    }

    /** @return 该等级的每秒物品上限是否为"无限"(MK16)。 */
    public boolean isItemLimitUnlimited() {
        return matter == Matter.FINAL;
    }

    /** @return 该等级的每秒流体上限是否为"无限"(MK16)。 */
    public boolean isFluidLimitUnlimited() {
        return matter == Matter.FINAL;
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

    /** @return 在线所有者(离线返回 null:此时 EMC 与物品都先攒着)。 */
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

    // 绑定物品 / NBT 过滤

    public ItemStack getBoundItem() {
        return bound == null ? null : bound.copy();
    }

    public boolean hasBoundItem() {
        return bound != null;
    }

    public void clearBoundItem() {
        bound = null;
        markDirty();
        notifyNeighbors();
    }

    /**
     * 绑定导出物品:过滤开启时把 NBT/耐久抹掉(与 PE 的"persistent info"一致),
     * 关掉过滤时按原样绑定。
     */
    public void bindItem(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return;
        }
        ItemStack copy = stack.copy();
        copy.stackSize = 1;
        if (isNbtFilterEnabled()) {
            copy.setTagCompound(null);
            if (copy.isItemStackDamageable()) {
                copy.setItemDamage(0);
            }
        }
        bound = copy;
        markDirty();
        notifyNeighbors();
    }

    /** 绑定/解绑后轻推邻居:AE 存储总线/管道据此重建视图,不用等它们自己下一次轮询。 */
    private void notifyNeighbors() {
        if (worldObj != null && !worldObj.isRemote) {
            worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());
        }
    }

    public boolean isNbtFilterEnabled() {
        if (worldObj == null) {
            return true;
        }
        return (worldObj.getBlockMetadata(xCoord, yCoord, zCoord) & 1) == 0;
    }

    /** @return 该物品是否能被这个接口收/发:有精确价,且符合 NBT 过滤规则。 */
    public boolean accepts(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return false;
        }
        if (!passesNbtFilter(stack)) {
            return false;
        }
        BigInteger price = PXEmcTable.get(stack);
        return price != null && price.signum() > 0;
    }

    private boolean passesNbtFilter(ItemStack stack) {
        if (!isNbtFilterEnabled()) {
            return true;
        }
        if (stack.getTagCompound() != null) {
            return false;
        }
        return !(stack.isItemStackDamageable() && stack.getItemDamage() != 0);
    }

    /** @return 绑定的物品若是装满的桶,返回其中的流体。 */
    public FluidStack getBoundFluid() {
        if (bound == null) {
            return null;
        }
        return FluidContainerRegistry.getFluidForFilledItem(bound);
    }

    public boolean hasBoundFluid() {
        return getBoundFluid() != null;
    }

    // 每秒结算

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        if (worldObj == null || worldObj.isRemote) {
            return;
        }
        absorbPending();
        if (++tickCounter < TICK_INTERVAL) {
            return;
        }
        tickCounter = 0;
        resetLimits();
        flushEmc();
    }

    /** 把缓冲的 EMC 注入所有者;所有者离线时留着。 */
    private void flushEmc() {
        if (bufferedEmc.signum() <= 0) {
            return;
        }
        EntityPlayer owner = getOwnerPlayer();
        if (owner == null) {
            return;
        }
        BigInteger toAdd = bufferedEmc;
        bufferedEmc = BigInteger.ZERO;
        PXEmcAccess.addEmcBig(owner, toAdd);
        markDirty();
    }

    // 接收 EMC(相邻继电器/收集器推过来)

    @Override
    public BigInteger acceptEmcBig(ForgeDirection side, BigInteger toAccept) {
        if (toAccept == null || toAccept.signum() <= 0) {
            return BigInteger.ZERO;
        }
        BigInteger room = remainingEmc == null ? toAccept : remainingEmc.min(toAccept);
        if (room.signum() <= 0) {
            return BigInteger.ZERO;
        }
        bufferedEmc = bufferedEmc.add(room);
        if (remainingEmc != null) {
            remainingEmc = remainingEmc.subtract(room);
        }
        markDirty();
        return room;
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
    public double getStoredEmc() {
        return bufferedEmc.doubleValue();
    }

    @Override
    public double getMaximumEmc() {
        BigInteger limit = matter.getEmcLinkEmcLimit();
        return limit == null ? Double.MAX_VALUE : limit.doubleValue();
    }

    /** @return 当前缓冲里等着注入所有者的 EMC。 */
    public BigInteger getBufferedEmc() {
        return bufferedEmc;
    }

    // 导入:物品 -> 所有者的 EMC

    private void absorbPending() {
        if (pending == null || pending.stackSize <= 0) {
            pending = null;
            return;
        }
        if (!accepts(pending)) {
            pending = null;
            markDirty();
            return;
        }
        EntityPlayer owner = getOwnerPlayer();
        if (owner == null) {
            // 所有者离线:物品留在槽里,等他上线再转
            return;
        }
        int count = Math.min(pending.stackSize, Math.max(1, pending.getMaxStackSize()));
        if (matter != Matter.FINAL) {
            if (remainingImport <= 0) {
                return; // 这一秒的导入额度用完了,下一 tick 再试
            }
            count = Math.min(count, remainingImport);
        }
        BigInteger price = PXEmcTable.get(pending);
        if (price == null || price.signum() <= 0) {
            pending = null;
            markDirty();
            return;
        }
        BigInteger total = price.multiply(BigInteger.valueOf(count));
        PXEmcAccess.addEmcBig(owner, total);
        learn(owner, pending);
        if (matter != Matter.FINAL) {
            remainingImport -= count;
        }
        pending.stackSize -= count;
        if (pending.stackSize <= 0) {
            pending = null;
        }
        markDirty();
    }

    private void learn(EntityPlayer owner, ItemStack stack) {
        ItemStack single = stack.copy();
        single.stackSize = 1;
        if (!Transmutation.hasKnowledgeForStack(single, owner)) {
            Transmutation.addKnowledge(single, owner);
        }
    }

    // 导出:所有者的 EMC -> 物品

    /**
     * @return 所有者当前买得起的绑定物品,数量 = 现在真正能抽走的量(EMC 买得起、每秒额度允许、
     *         且不超过该物品的最大堆叠);没绑定 / 额度用尽 / 所有者离线时返回 null。
     *
     *         <p>
     *         这里必须回报"现在能抽多少",而不是"买得起多少":管道(BC/AE/GT 等)通常先读这个槽位、
     *         再按读到的数量抽取;如果回报一个远超额度的数字,管道抽到 1 个就以为"只抽到这么点",
     *         随后的重试又被每秒额度挡住,观感就是"标称 1 个/秒,实际两秒才出一个"。
     */
    public ItemStack getExportStack() {
        int count = getExportableCountForPipe();
        if (count <= 0) {
            return null;
        }
        ItemStack out = bound.copy();
        out.stackSize = count;
        return out;
    }

    /** @return 按物品自身最大堆叠夹一下数量(非堆叠物品 = 1)。 */
    private int maxStackOf(ItemStack stack) {
        return Math.max(1, stack.getMaxStackSize());
    }

    /**
     * @return 现在还允许抽走多少个(EMC 买得起 + **分级每秒额度**,再夹到物品最大堆叠)。
     *
     *         <p>
     *         导出只能靠管道:管道的抽取一律受分级每秒额度约束(上游的 ItemHandler.extractItem 也是硬限制)。
     */
    public int getExportableCountForPipe() {
        return Math.min(exportable(true), maxStackOf(bound));
    }

    /** @return 不考虑额度、只看买得起多少。 */
    private int exportable(boolean respectTierLimit) {
        if (bound == null) {
            return 0;
        }
        EntityPlayer owner = getOwnerPlayer();
        if (owner == null) {
            return 0;
        }
        BigInteger price = PXEmcTable.get(bound);
        if (price == null || price.signum() <= 0) {
            return 0;
        }
        int affordable = PXEmcAccess.getEmcBig(owner)
            .divide(price)
            .min(BigInteger.valueOf(Integer.MAX_VALUE))
            .intValue();
        if (matter != Matter.FINAL && respectTierLimit) {
            affordable = Math.min(affordable, Math.max(0, remainingExport));
        }
        return affordable;
    }

    /** @return 还能导出多少个(受分级额度和所有者 EMC 限制),供显示用。 */
    public int getExportableCount() {
        return getExportableCountForPipe();
    }

    /** 管道抽物品:受分级每秒额度硬限制,单次最多一叠。 */
    public ItemStack extractForPipe(int amount) {
        if (amount <= 0) {
            return null;
        }
        int take = Math.min(Math.min(amount, getExportableCountForPipe()), maxStackOf(bound));
        if (take <= 0) {
            return null;
        }
        return payAndTake(take);
    }

    /** 扣所有者 EMC 并造出 take 个绑定物品,同时扣掉这一秒的导出额度。 */
    private ItemStack payAndTake(int take) {
        if (take <= 0 || bound == null) {
            return null;
        }
        EntityPlayer owner = getOwnerPlayer();
        if (owner == null) {
            return null;
        }
        BigInteger price = PXEmcTable.get(bound);
        if (price == null || price.signum() <= 0) {
            return null;
        }
        PXEmcAccess.addEmcBig(
            owner,
            price.multiply(BigInteger.valueOf(take))
                .negate());
        if (matter != Matter.FINAL) {
            remainingExport -= take;
        }
        ItemStack out = bound.copy();
        out.stackSize = take;
        markDirty();
        return out;
    }

    // 流体:每 mB 的价格 = (整桶价 - 桶价 × 效率%) / 1000

    /** @return 每 mB 的价格(向下取整);绑定不是装满的桶时返回 null。 */
    public BigInteger getFluidCostPerBucket() {
        FluidStack fluid = getBoundFluid();
        if (fluid == null) {
            return null;
        }
        BigInteger full = PXEmcTable.get(bound);
        if (full == null || full.signum() < 0) {
            return null;
        }
        BigInteger bucketValue = PXEmcTable.getOrZero(new ItemStack(Items.bucket));
        BigInteger deduction = bucketValue.multiply(BigInteger.valueOf(matter.getFluidEfficiencyPercentage()))
            .divide(BigInteger.valueOf(100));
        BigInteger net = full.subtract(deduction);
        if (net.signum() < 0) {
            net = BigInteger.ZERO;
        }
        return net;
    }

    /** @return 每 mB 的 EMC 价(整数,不足 1 的按 0 处理 = 免费流体)。 */
    public BigInteger getFluidCostPerMb() {
        BigInteger perBucket = getFluidCostPerBucket();
        return perBucket == null ? null : perBucket.divide(BigInteger.valueOf(BUCKET_VOLUME));
    }

    /** @return 能抽出的流体的实际数量(按管道算:受分级每秒额度与所有者 EMC 限制)。 */
    public int getDrainableAmount(int maxDrain) {
        return getDrainableAmount(maxDrain, true);
    }

    /**
     * @param respectTierLimit 是否受分级每秒额度约束(管道 = true)
     * @return 能抽出的流体的实际数量(受额度与所有者 EMC 限制)
     */
    public int getDrainableAmount(int maxDrain, boolean respectTierLimit) {
        FluidStack fluid = getBoundFluid();
        if (fluid == null || maxDrain <= 0) {
            return 0;
        }
        int amount = maxDrain;
        if (matter != Matter.FINAL && respectTierLimit) {
            amount = Math.min(amount, Math.max(0, remainingFluid));
        }
        BigInteger costPer = getFluidCostPerMb();
        if (costPer != null && costPer.signum() > 0) {
            EntityPlayer owner = getOwnerPlayer();
            if (owner == null) {
                return 0;
            }
            int affordable = PXEmcAccess.getEmcBig(owner)
                .divide(costPer)
                .min(BigInteger.valueOf(Integer.MAX_VALUE))
                .intValue();
            amount = Math.min(amount, affordable);
        } else if (getOwnerPlayer() == null) {
            return 0;
        }
        return Math.max(0, amount);
    }

    /** 按 EMC 扣款并抽出最多 maxDrain mB 的绑定流体(按管道算);抽取失败返回 null。 */
    public FluidStack drainForPlayer(int maxDrain, boolean doDrain) {
        return drainForPlayer(maxDrain, doDrain, true);
    }

    /**
     * @param respectTierLimit 是否受分级每秒额度约束(管道 = true)
     * @return 抽出的流体;抽取失败返回 null
     */
    public FluidStack drainForPlayer(int maxDrain, boolean doDrain, boolean respectTierLimit) {
        FluidStack fluid = getBoundFluid();
        if (fluid == null) {
            return null;
        }
        int amount = getDrainableAmount(maxDrain, respectTierLimit);
        if (amount <= 0) {
            return null;
        }
        FluidStack out = fluid.copy();
        out.amount = amount;
        if (!doDrain) {
            return out;
        }
        EntityPlayer owner = getOwnerPlayer();
        if (owner == null) {
            return null;
        }
        BigInteger costPer = getFluidCostPerMb();
        if (costPer != null && costPer.signum() > 0) {
            PXEmcAccess.addEmcBig(
                owner,
                costPer.multiply(BigInteger.valueOf(amount))
                    .negate());
        }
        if (matter != Matter.FINAL && respectTierLimit) {
            remainingFluid -= amount;
        }
        markDirty();
        return out;
    }

    // IInventory

    @Override
    public int getSizeInventory() {
        return matter.getEmcLinkInventorySize();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot == EXPORT_SLOT) {
            return getExportStack();
        }
        // 导入槽是纯"上传口":上游对 slot != 0 一律回报空,管道才不会被误导成
        // "槽里已经堆着东西"(那样它们既不肯插入,又可能把还没转化的物品抽走)
        return null;
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (amount <= 0) {
            return null;
        }
        if (slot == EXPORT_SLOT) {
            // 管道抽物品:一律受分级每秒额度约束
            return extractForPipe(amount);
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot < IMPORT_SLOT || slot >= getSizeInventory()) {
            // 导出槽只出不进
            return;
        }
        if (stack == null || stack.stackSize <= 0) {
            pending = null;
            markDirty();
            return;
        }
        if (!accepts(stack)) {
            return;
        }
        // 先把上一次暂存的吃掉(同一 tick 连续插入也不丢件),再存新的
        absorbPending();
        pending = stack.copy();
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return "container.projectexpansiongtnh.emc_link";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        if (worldObj == null || worldObj.getTileEntity(xCoord, yCoord, zCoord) != this) {
            return false;
        }
        return player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64D;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return slot >= IMPORT_SLOT && slot < getSizeInventory() && accepts(stack);
    }

    // ISidedInventory

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        int size = getSizeInventory();
        int[] slots = new int[size];
        for (int i = 0; i < size; i++) {
            slots[i] = i;
        }
        return slots;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side) {
        return isItemValidForSlot(slot, stack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        // 只有导出槽能抽;导入槽一旦能抽就会出现"物品既变成了 EMC,又被管道拿回来"的复制漏洞
        return slot == EXPORT_SLOT && bound != null;
    }

    // IFluidHandler

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        // 接口不存流体
        return 0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        FluidStack fluid = getBoundFluid();
        if (resource == null || fluid == null || !fluid.isFluidEqual(resource)) {
            return null;
        }
        return drainForPlayer(resource.amount, doDrain);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        return drainForPlayer(maxDrain, doDrain);
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return false;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        FluidStack boundFluid = getBoundFluid();
        return boundFluid != null && (fluid == null || boundFluid.getFluid() == fluid);
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        FluidStack fluid = getBoundFluid();
        if (fluid == null) {
            return new FluidTankInfo[] { new FluidTankInfo(null, 0) };
        }
        int perSecond = matter.getEmcLinkFluidLimit();
        int available = matter == Matter.FINAL ? perSecond : Math.max(0, remainingFluid);
        // 容量视图不小于当前可抽量,避免出现 amount > capacity 的非法 FluidTankInfo
        int capacity = Math.max(perSecond, available);
        FluidStack view = fluid.copy();
        view.amount = available;
        return new FluidTankInfo[] { new FluidTankInfo(view, capacity) };
    }

    // 生命周期

    @Override
    public void invalidate() {
        bufferedEmc = BigInteger.ZERO;
        pending = null;
        super.invalidate();
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
        bound = compound.hasKey(NBT_BOUND) ? ItemStack.loadItemStackFromNBT(compound.getCompoundTag(NBT_BOUND)) : null;
        pending = compound.hasKey(NBT_PENDING) ? ItemStack.loadItemStackFromNBT(compound.getCompoundTag(NBT_PENDING))
            : null;
        bufferedEmc = BigInteger.ZERO;
        if (compound.hasKey(NBT_BUFFERED_EMC)) {
            try {
                bufferedEmc = new BigInteger(compound.getString(NBT_BUFFERED_EMC));
            } catch (NumberFormatException ignored) {
                bufferedEmc = BigInteger.ZERO;
            }
        }
        tickCounter = 0;
        resetLimits();
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setString(NBT_OWNER_NAME, ownerName == null ? "" : ownerName);
        if (ownerUUID != null) {
            compound.setString(NBT_OWNER, ownerUUID.toString());
        }
        if (bound != null) {
            NBTTagCompound tag = new NBTTagCompound();
            bound.writeToNBT(tag);
            compound.setTag(NBT_BOUND, tag);
        }
        if (pending != null && pending.stackSize > 0) {
            NBTTagCompound tag = new NBTTagCompound();
            pending.writeToNBT(tag);
            compound.setTag(NBT_PENDING, tag);
        }
        if (bufferedEmc.signum() != 0) {
            compound.setString(NBT_BUFFERED_EMC, bufferedEmc.toString());
        }
    }
}
