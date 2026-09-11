package com.projecte.expansion.tile;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;

import com.projecte.expansion.Config;
import com.projecte.expansion.registry.PXEmcAccess;
import com.projecte.expansion.registry.PXEmcTable;

import moze_intel.projecte.playerData.Transmutation;

/**
 * 转化接口(Transmutation Interface)的方块实体。
 *
 * <p>
 * 语义对齐上游 Project Expansion 的 {@code BlockEntityTransmutationInterface}:
 * <ul>
 * <li>槽位 0 = 输入:任何带 EMC 的物品放进来都会**立即**转化为所有者(团队共享时为团队)的 EMC,
 * 并学习该物品;物品本身不保存。</li>
 * <li>槽位 1..n = 所有者已学习的物品,每个槽报出的数量 = 所有者 EMC ÷ 物品价
 * (上限 {@link Config#transmutationInterfaceItemCount},默认 {@code Integer.MAX_VALUE});
 * 从这里取走物品会按**精确的 BigInteger 价**扣除 EMC。</li>
 * </ul>
 *
 * <p>
 * 对外的存储访问走 1.7.10 原生的 {@link IInventory}/{@link ISidedInventory}:AE2 的 ME 存储总线
 * (经 {@code StorageBusInventoryHandler})与各类管道都能直接接上,不需要 AE 编译依赖。
 *
 * <p>
 * 物品价取自 {@link PXEmcTable}(BigInteger 精确值);所有者 EMC 受 ProjectE 存储类型限制是 double,
 * 转换集中在 {@link PXEmcAccess} 里(团队模式在那里被代理到团队池)。
 */
public class TileEntityTransmutationInterfacePX extends TileEntity implements IInventory, ISidedInventory {

    /** 输入槽:放入即转化,不保存物品。 */
    public static final int INPUT_SLOT = 0;

    private static final int REFRESH_INTERVAL = 20;
    private static final int ACCESSIBLE_LIMIT = 1 + 4096;

    private static final String NBT_OWNER = "Owner";
    private static final String NBT_OWNER_NAME = "OwnerName";
    private static final String NBT_INPUT = "Input";

    private static final ItemStack[] NO_KNOWLEDGE = new ItemStack[0];

    private UUID ownerUUID;
    private String ownerName = "";
    /**
     * 输入槽里**暂存**的物品(不直接消失):下一 tick 才转化为 EMC。
     * 这样管道/AE 能看到物品被接收,也不会在所有者离线时把物品吞掉。
     */
    private ItemStack input;
    private ItemStack[] knowledge = NO_KNOWLEDGE;
    private int[] accessibleSlots = new int[] { INPUT_SLOT };
    private long lastRefresh = Long.MIN_VALUE;
    private boolean knowledgeDirty = true;

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
        knowledgeDirty = true;
        markDirty();
    }

    public boolean isOwner(EntityPlayer player) {
        return player != null && ownerUUID != null && ownerUUID.equals(player.getUniqueID());
    }

    /** @return 在线所有者(离线时返回 null:此时接口对外表现为空)。 */
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

    @Override
    public boolean canUpdate() {
        return true;
    }

    // 知识缓存

    private void ensureRefreshed() {
        if (worldObj == null || worldObj.isRemote) {
            return;
        }
        long now = worldObj.getTotalWorldTime();
        if (!knowledgeDirty && now - lastRefresh < REFRESH_INTERVAL) {
            return;
        }
        lastRefresh = now;
        knowledgeDirty = false;

        EntityPlayer owner = getOwnerPlayer();
        if (owner == null) {
            knowledge = NO_KNOWLEDGE;
            accessibleSlots = new int[] { INPUT_SLOT };
            return;
        }

        List<ItemStack> learned = PXEmcAccess.getKnowledge(owner);
        if (learned == null || learned.isEmpty()) {
            knowledge = NO_KNOWLEDGE;
            accessibleSlots = new int[] { INPUT_SLOT };
            return;
        }

        int size = Math.min(learned.size(), ACCESSIBLE_LIMIT - 1);
        ItemStack[] copy = new ItemStack[size];
        int written = 0;
        for (int i = 0; i < learned.size() && written < size; i++) {
            ItemStack stack = learned.get(i);
            if (stack == null || stack.getItem() == null) {
                continue;
            }
            ItemStack single = stack.copy();
            single.stackSize = 1;
            copy[written++] = single;
        }
        if (written != size) {
            ItemStack[] trimmed = new ItemStack[written];
            System.arraycopy(copy, 0, trimmed, 0, written);
            copy = trimmed;
        }
        knowledge = copy;

        int[] slots = new int[copy.length + 1];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = i;
        }
        accessibleSlots = slots;
    }

    /** @return 该知识槽位所对应的物品(可能为 null)。 */
    public ItemStack getKnowledgeStack(int index) {
        ensureRefreshed();
        if (index < 0 || index >= knowledge.length) {
            return null;
        }
        return knowledge[index];
    }

    public int getKnowledgeCount() {
        ensureRefreshed();
        return knowledge.length;
    }

    /** 输入槽是否接受该物品:NBT 过滤规则 + 必须能求到价。 */
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

    /**
     * 红色模式(方块元数据 0,默认)启用 NBT 过滤:带 NBT 或已消耗耐久的物品不接收;
     * 蓝色模式(元数据 1)忽略一切 NBT,按物品基础价转化。
     */
    private boolean passesNbtFilter(ItemStack stack) {
        if (!isNbtFilterEnabled()) {
            return true;
        }
        if (stack.getTagCompound() != null) {
            return false;
        }
        return !(stack.isItemStackDamageable() && stack.getItemDamage() != 0);
    }

    public boolean isNbtFilterEnabled() {
        if (worldObj == null) {
            return true;
        }
        return (worldObj.getBlockMetadata(xCoord, yCoord, zCoord) & 1) == 0;
    }

    // 价格与数量

    /** @return 单个物品的精确价(null 表示无价)。 */
    private BigInteger priceOf(ItemStack stack) {
        return PXEmcTable.get(stack);
    }

    /** @return 第 index 个知识物品当前可被取出的最大数量(EMC ÷ 价,并受配置上限约束)。 */
    private int maxBuyable(int index) {
        if (index < 0 || index >= knowledge.length) {
            return 0;
        }
        EntityPlayer owner = getOwnerPlayer();
        if (owner == null) {
            return 0;
        }
        BigInteger price = priceOf(knowledge[index]);
        if (price == null || price.signum() <= 0) {
            return 0;
        }
        // 精确取余额:团队模式(装了 PE-E-GTNH 的 ProjectE-Team)直接拿 BigInteger 团队池
        BigInteger emc = PXEmcAccess.getEmcBig(owner);
        if (emc.signum() <= 0) {
            return 0;
        }
        int cap = Math.max(1, Config.transmutationInterfaceItemCount);
        BigInteger count = emc.divide(price)
            .min(BigInteger.valueOf(cap));
        return count.intValue();
    }

    // 输入:物品 → EMC

    private void absorb(ItemStack stack, EntityPlayer owner) {
        BigInteger price = priceOf(stack);
        if (price == null || price.signum() <= 0) {
            return;
        }
        // 夹到物品自身的最大堆叠:外部(管道/AE/异常 NBT)可能传来超大 stackSize,
        // 不夹的话一笔就能把 EMC 顶到天文数字
        int count = Math.min(stack.stackSize, Math.max(1, stack.getMaxStackSize()));
        BigInteger total = price.multiply(BigInteger.valueOf(count));
        PXEmcAccess.addEmcBig(owner, total);
        learn(owner, stack);
        markDirty();
    }

    private void learn(EntityPlayer owner, ItemStack stack) {
        ItemStack single = stack.copy();
        single.stackSize = 1;
        if (!Transmutation.hasKnowledgeForStack(single, owner)) {
            Transmutation.addKnowledge(single, owner);
            // ProjectE-Team 的团队代理会把这次写入落到团队知识列表里
            knowledgeDirty = true;
            markDirty();
        }
    }

    // IInventory

    @Override
    public int getSizeInventory() {
        ensureRefreshed();
        return knowledge.length + 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot == INPUT_SLOT) {
            return input == null ? null : input.copy();
        }
        ensureRefreshed();
        if (slot <= INPUT_SLOT || slot > knowledge.length) {
            return null;
        }
        int count = maxBuyable(slot - 1);
        if (count <= 0) {
            return null;
        }
        ItemStack out = knowledge[slot - 1].copy();
        out.stackSize = count;
        return out;
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (amount <= 0) {
            return null;
        }
        if (slot == INPUT_SLOT) {
            if (input == null || input.stackSize <= 0) {
                return null;
            }
            int take = Math.min(amount, input.stackSize);
            ItemStack out = input.copy();
            out.stackSize = take;
            input.stackSize -= take;
            if (input.stackSize <= 0) {
                input = null;
            }
            markDirty();
            return out;
        }
        ensureRefreshed();
        if (slot > knowledge.length) {
            return null;
        }
        EntityPlayer owner = getOwnerPlayer();
        if (owner == null) {
            return null;
        }
        int take = Math.min(amount, maxBuyable(slot - 1));
        if (take <= 0) {
            return null;
        }
        BigInteger price = priceOf(knowledge[slot - 1]);
        if (price == null || price.signum() <= 0) {
            return null;
        }
        BigInteger total = price.multiply(BigInteger.valueOf(take));
        PXEmcAccess.addEmcBig(owner, total.negate());
        ItemStack out = knowledge[slot - 1].copy();
        out.stackSize = take;
        markDirty();
        return out;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot != INPUT_SLOT) {
            // 只有输入槽接收物品,知识槽是只读的
            return;
        }
        ensureRefreshed();
        if (stack == null || stack.stackSize <= 0) {
            input = null;
            markDirty();
            return;
        }
        if (!accepts(stack)) {
            return;
        }
        // 先把暂存的那一份转化掉(同一 tick 内连续插入也不会丢件),再存新的
        absorbInput();
        input = stack.copy();
        markDirty();
    }

    /** 把输入槽里暂存的物品转化为 EMC 并学习;所有者离线或物品不合法时保留/丢弃由调用点决定。 */
    private void absorbInput() {
        if (input == null || input.stackSize <= 0) {
            input = null;
            return;
        }
        if (!accepts(input)) {
            input = null;
            markDirty();
            return;
        }
        EntityPlayer owner = getOwnerPlayer();
        if (owner == null) {
            // 所有者离线:物品留在输入槽里,等他上线再转化(不吞物品)
            return;
        }
        absorb(input, owner);
        input = null;
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return "container.projectexpansiongtnh.transmutation_interface";
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
    public void markDirty() {
        super.markDirty();
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
        return slot == INPUT_SLOT && accepts(stack);
    }

    // ISidedInventory

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        ensureRefreshed();
        return accessibleSlots;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side) {
        return slot == INPUT_SLOT && accepts(stack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return slot != INPUT_SLOT;
    }

    // 生命周期

    @Override
    public void updateEntity() {
        if (worldObj == null || worldObj.isRemote) {
            return;
        }
        ensureRefreshed();
        absorbInput();
    }

    @Override
    public void invalidate() {
        knowledge = NO_KNOWLEDGE;
        knowledgeDirty = true;
        lastRefresh = Long.MIN_VALUE;
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
        knowledgeDirty = true;
        lastRefresh = Long.MIN_VALUE;
        // 暂存物品持久化:区块卸载/重启时不丢件
        input = compound.hasKey(NBT_INPUT) ? ItemStack.loadItemStackFromNBT(compound.getCompoundTag(NBT_INPUT)) : null;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setString(NBT_OWNER_NAME, ownerName == null ? "" : ownerName);
        if (ownerUUID != null) {
            compound.setString(NBT_OWNER, ownerUUID.toString());
        }
        if (input != null && input.stackSize > 0) {
            NBTTagCompound inputTag = new NBTTagCompound();
            input.writeToNBT(inputTag);
            compound.setTag(NBT_INPUT, inputTag);
        }
    }
}
