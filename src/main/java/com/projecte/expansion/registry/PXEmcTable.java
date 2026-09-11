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

    private static final Map<Item, BigInteger> TABLE = new HashMap<>();
    private static final List<IPXEmcProvider> PROVIDERS = Collections.synchronizedList(new ArrayList<IPXEmcProvider>());

    private PXEmcTable() {}

    public static void put(Item item, BigInteger value) {
        if (item != null && value != null && value.signum() > 0) {
            TABLE.put(item, value);
        }
    }

    /**
     * 为其他 mod 的物品注册精确值(AutoEMC-GTNH 的用例):该值会成为本 mod 机器以及 ProjectE
     * 转化桌 / 冷凝器运行时补丁的权威来源,同时(夹取后)也会写入 ProjectE 自己的 int 表,
     * 好让仍读取它的 ProjectE 逻辑看到合理的数值。
     */
    public static void register(Item item, BigInteger value) {
        if (item == null || value == null || value.signum() <= 0) return;
        put(item, value);
        APICustomEMCMapper.instance.registerCustomEMC(new ItemStack(item, 1, 0), toPeInt(value));
    }

    /** 添加一个惰性数值来源;见 {@link IPXEmcProvider}。 */
    public static void addProvider(IPXEmcProvider provider) {
        if (provider != null && !PROVIDERS.contains(provider)) {
            PROVIDERS.add(provider);
        }
    }

    /** @return 当某个 provider(而非本 mod 自己的表)为该物品定价时返回 true。 */
    public static boolean isProvidedByOtherMod(ItemStack stack) {
        return stack != null && stack.getItem() != null
            && TABLE.get(stack.getItem()) == null
            && findInProviders(stack) != null;
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
        BigInteger own = TABLE.get(stack.getItem());
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
        return new BigDecimal(value).min(BigDecimal.valueOf(Double.MAX_VALUE))
            .doubleValue();
    }

    public static BigInteger fromPe(double value) {
        return BigDecimal.valueOf(value)
            .toBigInteger();
    }
}
