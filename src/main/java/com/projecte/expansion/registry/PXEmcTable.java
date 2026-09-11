package com.projecte.expansion.registry;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.projecte.expansion.api.IPXEmcProvider;

import moze_intel.projecte.emc.mappers.APICustomEMCMapper;
import moze_intel.projecte.utils.EMCHelper;

/**
 * 本 mod 自有物品的权威 BigInteger EMC 表。
 *
 * <p>
 * 上游 Project Expansion 用 BigInteger 存储 EMC(机器缓冲区、上限与输出远超 long——可达约 1e60),
 * 而 ProjectE 1.7.10 只认 int 的物品价值({@code EMCMapper.emc})与 double 的玩家 EMC
 * ({@code Transmutation.getEmc})。因此本表是我们这边的唯一精确值来源;每当数值进入 PE,
 * 都会用 {@link #toPeInt} / {@link #toPeDouble} 收窄。
 *
 * <p>
 * 两张表:
 * <ul>
 * <li>{@link #TABLE}(按 {@link Item})—— 本 mod 自己的物品,一个物品一个值;</li>
 * <li>{@link #STACK_TABLE}(按 item + 元数据 + NBT)—— 外部求值器(如 AutoEMC-GTNH)注册的值。
 * GT 的 metaitem 全部靠 damage 区分,把它们的值按 Item 存会把成百上千个变体压成一条,
 * 并让 ProjectE 侧只看到 damage 0 的那一个(表现为"大量物品失去价格")。</li>
 * </ul>
 *
 * <p>
 * M3 的机器(收集器、继电器、EMC 接口、力量花)必须把缓冲区保持为 BigInteger,并走本类而不是
 * PE 的 int 表。其他 mod 应通过 {@link com.projecte.expansion.api.PXEmcValues} 读取数值,
 * 而不要直接触碰本表。
 */
public final class PXEmcTable {

    /**
     * 本 mod 认可的 EMC 上限:ProjectE 把玩家/团队 EMC 存成 double,所以上限就是 double 满量程。
     * (PE 自己的 2^30 夹取与 ProjectE-Team 团队池的 2^30 夹取都已由本 mod 的 mixin / 写路径绕过。)
     */
    public static final double PE_EMC_MAX = Double.MAX_VALUE;

    /**
     * 单件物品的价格上限。上游 Project Expansion 的数值量级约 1e60,而 ProjectE 的玩家 EMC 是
     * double(约 1.8e308):这里给外部求值器(AutoEMC-GTNH 的无上限 BigInteger 引擎)留出极大空间,
     * 同时避免"一件物品的价本身超出 double 域"—— 那样独立玩家(非团队模式)卖一件东西就会把余额
     * 顶到 {@link Double#MAX_VALUE},看起来像 double 溢出。
     */
    public static final BigInteger PRICE_CAP = BigInteger.TEN.pow(60);

    private static final Map<Item, BigInteger> TABLE = new HashMap<>();
    /** 栈级表:物品 + 元数据 + NBT(精确匹配,含 NBT 的物品用它) */
    private static final Map<StackKey, BigInteger> STACK_TABLE = new HashMap<>();
    /** 元数据级表:只按 物品 + damage(兜底:NBT 不同/带充能等状态的同一变体也能查到价) */
    private static final Map<StackKey, BigInteger> DAMAGE_TABLE = new HashMap<>();
    private static final List<IPXEmcProvider> PROVIDERS = Collections.synchronizedList(new ArrayList<IPXEmcProvider>());

    private PXEmcTable() {}

    public static void put(Item item, BigInteger value) {
        if (item != null && value != null && value.signum() > 0) {
            TABLE.put(item, cap(value));
        }
    }

    /**
     * 为其他 mod 的物品注册精确值(AutoEMC-GTNH 的用例):该值会成为本 mod 机器以及 ProjectE
     * 转化桌 / 冷凝器运行时补丁的权威来源,同时(夹取后)也会写入 ProjectE 自己的 int 表,
     * 好让仍读取它的 ProjectE 逻辑看到合理的数值。
     */
    public static void register(Item item, BigInteger value) {
        if (item == null || value == null || value.signum() <= 0) return;
        BigInteger capped = cap(value);
        put(item, capped);
        APICustomEMCMapper.instance.registerCustomEMC(new ItemStack(item, 1, 0), toPeInt(capped));
    }

    /**
     * 按**完整物品栈**(物品 + 元数据 + NBT)注册精确值。
     *
     * <p>
     * 必须用这个重载而不是 {@link #register(Item, BigInteger)}:ProjectE 的表是按 item+damage+NBT
     * 索引的,写死 damage 0 会让 GT metaitem 的其它变体在 ProjectE 侧完全没有价。
     */
    public static void register(ItemStack stack, BigInteger value) {
        if (stack == null || stack.getItem() == null || value == null || value.signum() <= 0) return;
        BigInteger capped = cap(value);
        ItemStack single = stack.copy();
        single.stackSize = 1;
        STACK_TABLE.put(new StackKey(single, true, true), capped);
        ItemStack noNbt = single.copy();
        noNbt.setTagCompound(null);
        DAMAGE_TABLE.put(new StackKey(noNbt, false, false), capped);
        // 镜像进 PE:带上原物品的元数据/NBT,否则只有 damage 0 的变体能在 PE 里查到价
        APICustomEMCMapper.instance.registerCustomEMC(single, toPeInt(capped));
    }

    /** 添加一个惰性数值来源;见 {@link IPXEmcProvider}。 */
    public static void addProvider(IPXEmcProvider provider) {
        if (provider != null && !PROVIDERS.contains(provider)) {
            PROVIDERS.add(provider);
        }
    }

    /** @return 当某个 provider(而非本 mod 自己的表)为该物品定价时返回 true。 */
    public static boolean isProvidedByOtherMod(ItemStack stack) {
        return stack != null && stack.getItem() != null && ownValue(stack) == null && findInProviders(stack) != null;
    }

    /** 本 mod 的表(栈级或物品级)里该物品的值,没有则 null(不问 provider / PE)。 */
    private static BigInteger ownValue(ItemStack stack) {
        if (!STACK_TABLE.isEmpty()) {
            BigInteger byStack = STACK_TABLE.get(new StackKey(stack, false, true));
            if (byStack != null) return byStack;
        }
        if (!DAMAGE_TABLE.isEmpty()) {
            BigInteger byDamage = DAMAGE_TABLE.get(new StackKey(stack, false, false));
            if (byDamage != null) return byDamage;
        }
        return TABLE.get(stack.getItem());
    }

    private static BigInteger findInProviders(ItemStack stack) {
        if (PROVIDERS.isEmpty()) return null;
        IPXEmcProvider[] snapshot;
        synchronized (PROVIDERS) {
            snapshot = PROVIDERS.toArray(new IPXEmcProvider[0]);
        }
        for (IPXEmcProvider provider : snapshot) {
            try {
                BigInteger value = provider.getEmcValue(stack);
                if (value != null && value.signum() > 0) return value;
            } catch (Throwable t) {
                // 行为异常的 provider 绝不能破坏其他所有东西的定价。
            }
        }
        return null;
    }

    /** @return 我们的精确值、PE 值转成的 BigInteger,或该物品无值时返回 null。 */
    public static BigInteger get(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return null;
        BigInteger own = ownValue(stack);
        if (own != null) return own;
        BigInteger provided = findInProviders(stack);
        if (provided != null) return provided;
        if (EMCHelper.doesItemHaveEmc(stack)) {
            return BigInteger.valueOf(EMCHelper.getEmcValue(stack));
        }
        return null;
    }

    public static BigInteger getOrZero(ItemStack stack) {
        BigInteger v = get(stack);
        return v == null ? BigInteger.ZERO : v;
    }

    public static boolean has(ItemStack stack) {
        return get(stack) != null;
    }

    /** 我们算出的精确值,忽略 PE(当我们未给该物品定价时为 null)。 */
    public static BigInteger getOwn(Item item) {
        return TABLE.get(item);
    }

    /** 我们精确值的实时视图(由公共 API 的 snapshot() 使用)。 */
    public static Map<Item, BigInteger> ownValues() {
        return TABLE;
    }

    /** 夹到本 mod 认可的价格上限;见 {@link #PRICE_CAP}。 */
    public static BigInteger cap(BigInteger value) {
        if (value == null) return null;
        return value.compareTo(PRICE_CAP) > 0 ? PRICE_CAP : value;
    }

    /** PE 1.7.10 以 int 存储物品价值;更大的值会饱和。 */
    public static int toPeInt(BigInteger value) {
        if (value == null) return 0;
        return value.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0 ? Integer.MAX_VALUE : value.intValue();
    }

    /**
     * PE 1.7.10 以 double 存储玩家 EMC(可用范围约到 1.8e308),因此 BigInteger 数值是可以表示的——
     * 我们只防范荒谬的量级。
     */
    public static double toPeDouble(BigInteger value) {
        if (value == null) return 0D;
        BigDecimal capped = new BigDecimal(cap(value));
        return capped.min(BigDecimal.valueOf(Double.MAX_VALUE))
            .doubleValue();
    }

    public static BigInteger fromPe(double value) {
        if (Double.isNaN(value) || value <= 0D) return BigInteger.ZERO;
        if (Double.isInfinite(value)) return PRICE_CAP;
        return BigDecimal.valueOf(value)
            .toBigInteger();
    }

    /**
     * 精确表的栈级键:物品 + 元数据 + NBT。
     *
     * <p>
     * 哈希只用物品与元数据(便宜),equals 再做一次完整的物品栈比较:哈希碰撞只是落在同一个桶里,
     * 不会把两个不同的变体混为一谈。
     */
    private static final class StackKey {

        private final ItemStack stack;
        /** 是否比较 NBT(栈级表 true;元数据级表 false,并在哈希里区分两张表) */
        private final boolean compareNbt;

        /**
         * @param copy       true 时保存一份快照(写入用);false 时只借用调用方的栈(查询用,不复制)
         * @param compareNbt 是否把 NBT 也算进匹配条件
         */
        private StackKey(ItemStack stack, boolean copy, boolean compareNbt) {
            if (copy) {
                ItemStack snapshot = stack.copy();
                snapshot.stackSize = 1;
                this.stack = snapshot;
            } else {
                this.stack = stack;
            }
            this.compareNbt = compareNbt;
        }

        @Override
        public int hashCode() {
            return 31 * System.identityHashCode(stack.getItem()) + stack.getItemDamage() + (compareNbt ? 1 : 0);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof StackKey)) return false;
            StackKey key = (StackKey) other;
            if (compareNbt != key.compareNbt) return false;
            ItemStack a = stack;
            ItemStack b = key.stack;
            // 必须用 areItemStackTagsEqual(只比 NBT)而不是 areItemStacksEqual:后者连 stackSize 一起比,
            // 而注册时栈被归一化成 size=1,于是"手里拿着 64 个去查价"会查不到而回落到 PE 的 int 表(21 亿)
            return a.getItem() == b.getItem() && a.getItemDamage() == b.getItemDamage()
                && (!compareNbt || ItemStack.areItemStackTagsEqual(a, b));
        }
    }
}
