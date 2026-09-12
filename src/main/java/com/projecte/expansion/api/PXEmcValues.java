package com.projecte.expansion.api;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import com.projecte.expansion.registry.PXEmcTable;
import com.projecte.expansion.util.EMCFormatPX;

/**
 * 本 mod 的公开 BigInteger EMC API:ProjectE 在所有版本中都用 int 存储物品 EMC
 * (上限 2,147,483,647)——上游 Project Expansion 的应对办法是只在自己的机器里保留
 * BigInteger。本类对外暴露本 mod 计算出的精确值,以便其他 mod(AutoEMC-GTNH、
 * ProjectE-Team 等)无需改动 ProjectE 即可读取。
 *
 * <p>
 * 数值在 {@code FMLServerStartedEvent} 期间填充(因此在专用服务器上服务端可用,
 * 单人模式下两端共享同一 JVM)。访问是空值安全的,并会回退到 ProjectE 自身的
 * (已夹取的)值。对反射友好:所有方法都是 static 且 public,API 版本常量让
 * 调用方可以探测该接口。
 */
public final class PXEmcValues {

    /** 当方法签名或查找方式变化时递增;调用方可通过反射检查。 */
    public static final String API_VERSION = "2";

    private static final BigInteger PE_INT_CAP = BigInteger.valueOf(Integer.MAX_VALUE);

    private PXEmcValues() {}

    /** @return 当本 mod 或 ProjectE 对该物品有值时返回 true。 */
    public static boolean hasValue(ItemStack stack) {
        return PXEmcTable.get(stack) != null;
    }

    /** @return 精确的 BigInteger 值,无值时返回 null。 */
    public static BigInteger getValue(ItemStack stack) {
        return PXEmcTable.get(stack);
    }

    public static BigInteger getValueOrZero(ItemStack stack) {
        return PXEmcTable.getOrZero(stack);
    }

    /** @return 本 mod 为该物品计算的值(不回退 ProjectE),无值时返回 null。 */
    public static BigInteger getOwnValue(Item item) {
        return PXEmcTable.getOwn(item);
    }

    /**
     * 为物品登记精确值(API 的写入方向,供 AutoEMC-GTNH 使用):该值对本
     * mod 的机器以及 ProjectE 转化桌与冷凝器的运行时补丁具有权威性,并
     * 被夹取为 int 后镜像进 ProjectE 自己的表,以便 ProjectE 的机制仍能
     * 看到一个值。在 preInit 期间或之后调用都安全;重复登记同一物品会
     * 覆盖先前的值。
     */
    public static void register(Item item, BigInteger value) {
        PXEmcTable.register(item, value);
    }

    /**
     * 按完整物品栈(物品 + 元数据 + NBT)登记精确值。传入带元数据的栈时**必须**用这个重载:
     * {@link #register(Item, BigInteger)} 只覆盖 damage 0,会把 GT metaitem 的其它变体丢价。
     */
    public static void register(ItemStack stack, BigInteger value) {
        PXEmcTable.register(stack, value);
    }

    /**
     * 添加一个惰性值来源,而不必逐个物品登记;见 {@link IPXEmcProvider}。所提供的值会被
     * 转化桌/冷凝器补丁以及 {@link #getValue(ItemStack)} 使用,并优先于 ProjectE 那张
     * 以 int 封顶的表被查询。
     */
    public static void addProvider(IPXEmcProvider provider) {
        PXEmcTable.addProvider(provider);
    }

    /** @return 物品的精确值(不区分 NBT/元数据变体),无值时返回 null。 */
    public static BigInteger getValue(Item item) {
        return PXEmcTable.getOwn(item);
    }

    /** @return 本 mod 计算出的全部值的不可变快照。 */
    public static Map<Item, BigInteger> snapshot() {
        return Collections.unmodifiableMap(new HashMap<>(PXEmcTable.ownValues()));
    }

    /** @return 当精确值无法放入 ProjectE 的 int 表时返回 true。 */
    public static boolean exceedsProjectE(ItemStack stack) {
        BigInteger v = getValue(stack);
        return v != null && v.compareTo(PE_INT_CAP) > 0;
    }

    /** 简短缩写文本(2.72Qi、1.45M …)。 */
    public static String format(BigInteger value) {
        return value == null ? "0" : EMCFormatPX.format(value);
    }

    /** 带千分位的精确文本(2,720,389,434,207,968,576)。 */
    public static String formatExact(BigInteger value) {
        return value == null ? "0"
            : NumberFormat.getNumberInstance(Locale.US)
                .format(value);
    }

    /** 收窄到 ProjectE 1.7.10 的 int EMC 表所能存储的范围。 */
    public static int toProjectEInt(BigInteger value) {
        return PXEmcTable.toPeInt(value);
    }

    /** 收窄到 ProjectE 的 double 玩家 EMC 存储(保留大数值)。 */
    public static double toProjectEDouble(BigInteger value) {
        return PXEmcTable.toPeDouble(value);
    }

    public static BigInteger fromProjectEDouble(double value) {
        return BigDecimal.valueOf(value)
            .toBigInteger();
    }

    /**
     * 追加本 mod 曾经为物品添加的"精确 EMC:"tooltip 行。
     *
     * <p>
     * **本 mod 的物品已不再调用它**:ProjectE 自己会给有价物品加 {@code EMC: 缩写 (精确值)}
     * 一行(由 {@link com.projecte.expansion.mixin.ToolTipEventMixin} 补上精确值),
     * 再加这一行就是同一个数值显示两遍。方法保留仅为兼容已有调用方。
     */
    @SuppressWarnings("unchecked")
    public static void addTooltip(ItemStack stack, List<String> lines) {
        BigInteger value = getValue(stack);
        if (value == null || value.signum() <= 0) {
            return;
        }
        // 仅当 ProjectE 自己那行按 int 封顶的结果会出错时才添加:对 ProjectE 已能正确
        // 显示的数值再补一行只会让 tooltip 重复。
        if (value.compareTo(PE_INT_CAP) > 0) {
            lines.add(
                StatCollector.translateToLocalFormatted(
                    "item.projectexpansion.emc.tooltip_exact",
                    format(value),
                    formatExact(value)));
        }
    }
}
