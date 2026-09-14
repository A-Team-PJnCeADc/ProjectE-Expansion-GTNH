package com.projecte.expansion.util;

import java.math.BigDecimal;
import java.math.BigInteger;

import net.minecraft.item.EnumRarity;

import com.projecte.expansion.Config;

/**
 * Project Expansion 的 16 个物质层级。BASIC/DARK/RED 复用 ProjectE 的内容
 * (炼金煤炭 / 暗物质 / 红物质),FINAL 复用终极之星碎片,
 * 与上游的 hasItem/hasBlock 规则保持一致。
 */
public enum Matter {

    BASIC(0),
    DARK(2),
    RED(4),
    MAGENTA(4),
    PINK(5),
    PURPLE(5),
    VIOLET(6),
    BLUE(6),
    CYAN(7),
    GREEN(7),
    LIME(8),
    YELLOW(8),
    ORANGE(9),
    WHITE(9),
    FADING(10),
    FINAL(10);

    public static final Matter[] VALUES = values();

    /**
     * 各层级收集器每秒产生的 EMC(未乘配置倍率),按下标 = MK1(MK1..MK16)排列:
     *
     * <pre>
     * MK1 BASIC 4            MK6 PURPLE 2,560          MK11 LIME 2,621,440          MK16 FINAL 1,000,000,000,000
     * MK2 DARK 12            MK7 VIOLET 10,240          MK12 YELLOW 10,485,760
     * MK3 RED 40             MK8 BLUE 40,960            MK13 ORANGE 41,943,040
     * MK4 MAGENTA 160        MK9 CYAN 163,840           MK14 WHITE 167,772,160
     * MK5 PINK 640           MK10 GREEN 655,360         MK15 FADING 671,088,640
     * </pre>
     *
     * <p>
     * MK11 起给的是精确的 ×4 值(表里的 2,620,000 / 10,480,000 / 41,940,000 / 167,770,000 / 671,080,000
     * 是它们的截断写法);只有 MK16 不走 ×4,直接是 1e12。
     *
     * <p>
     * 放在内部类里:枚举常量构造时枚举自身的静态字段还没初始化,直接引用会编译报错。
     */
    private static final class CollectorOutput {

        private static final long[] VALUES = { 4L, 12L, 40L, 160L, 640L, 2_560L, 10_240L, 40_960L, 163_840L, 655_360L,
            2_621_440L, 10_485_760L, 41_943_040L, 167_772_160L, 671_088_640L, 1_000_000_000_000L };
    }

    /**
     * 各层级继电器每秒发放的 EMC 加成(未乘配置倍率),按下标 = MK1(MK1..MK16)排列:
     *
     * <pre>
     * MK1 1          MK5 150          MK9 60,000          MK13 15,360,000
     * MK2 3          MK6 750          MK10 240,000        MK14 61,440,000
     * MK3 10         MK7 3,750        MK11 960,000        MK15 245,760,000
     * MK4 40         MK8 15,000       MK12 3,840,000     MK16 1,000,000,000,000
     * </pre>
     */
    private static final class RelayBonus {

        private static final long[] VALUES = { 1L, 3L, 10L, 40L, 150L, 750L, 3_750L, 15_000L, 60_000L, 240_000L,
            960_000L, 3_840_000L, 15_360_000L, 61_440_000L, 245_760_000L, 1_000_000_000_000L };
    }

    /**
     * 各层级继电器每秒能传输的 EMC 上限(未乘配置倍率),按下标 = MK1(MK1..MK16)排列:
     *
     * <pre>
     * MK1 64         MK5 10,240       MK9 2,621,440       MK13 671,088,640
     * MK2 192        MK6 40,960       MK10 10,485,760     MK14 2,684,354,560
     * MK3 640        MK7 163,840      MK11 41,943,040     MK15 10,737,418,240
     * MK4 2,560      MK8 655,360      MK12 167,772,160    MK16 9,223,372,036,854,775,807(Long.MAX_VALUE)
     * </pre>
     *
     * <p>
     * 表里的 2,620,000 / 10,480,000 / 41,940,000 / 167,770,000 / 671,080,000 / 2,680,000,000 / 10,730,000,000
     * 是这批 ×4 值的截断写法;MK16 表里的 9,220,000,000,000,000,000 就是 {@code Long.MAX_VALUE} —— 本 mod 的
     * 继电器存量本身是 BigInteger,这个上限只是"每秒传得动多少"。
     */
    private static final class RelayTransfer {

        private static final long[] VALUES = { 64L, 192L, 640L, 2_560L, 10_240L, 40_960L, 163_840L, 655_360L,
            2_621_440L, 10_485_760L, 41_943_040L, 167_772_160L, 671_088_640L, 2_684_354_560L, 10_737_418_240L,
            Long.MAX_VALUE };
    }

    /**
     * 各层级力量花每 20 tick(1 秒)产生的 EMC,按下标 = MK1(MK1..MK16)排列;
     * 就是"18 个同级收集器 + 30 个同级继电器"的产量(上游公式:18×收集器产出 + 30×继电器加成)。
     *
     * <pre>
     * MK1 102          MK5 132,192        MK9 171,320,832       MK13 222,031,798,272
     * MK2 612          MK6 793,152        MK10 1,027,924,992    MK14 1,332,190,789,632
     * MK3 3,672        MK7 4,758,912      MK11 6,167,549,952    MK15 7,993,144,737,792
     * MK4 22,032       MK8 28,553,472     MK12 37,005,299,712   MK16 47,958,868,426,752
     * </pre>
     *
     * <p>
     * 表里 MK9 的 171,320,852、MK14 的 1,332,290,789,632、MK16 的 47,958,864,426,752 是笔误
     * (它们的邻行本身就否掉了这三个数):这里统一取严格的 ×6 链。
     */
    private static final class PowerFlowerOutput {

        private static final long[] VALUES = { 102L, 612L, 3_672L, 22_032L, 132_192L, 793_152L, 4_758_912L, 28_553_472L,
            171_320_832L, 1_027_924_992L, 6_167_549_952L, 37_005_299_712L, 222_031_798_272L, 1_332_190_789_632L,
            7_993_144_737_792L, 47_958_868_426_752L };
    }

    /** 该层级收集器每秒的基础 EMC 产出(未乘配置(config)倍率)。 */
    public final long collectorOutput;
    /** 该层级继电器每秒发放的 EMC 加成(未乘配置(config)倍率)。 */
    public final long relayBonus;
    /** 该层级继电器每秒可转移的最大 EMC(未乘配置(config)倍率)。 */
    public final long relayTransfer;
    /** 该层级力量花每 20 tick 产生的 EMC(未乘配置(config)倍率)。 */
    public final long powerFlowerOutput;
    /** 上游的流体导出效率档位(逐级累加,见 getFluidEfficiencyPercentage)。 */
    private final int fluidEfficiency;
    public final int level;

    Matter(int fluidEfficiency) {
        this.level = ordinal() + 1;
        this.collectorOutput = CollectorOutput.VALUES[ordinal()];
        this.relayBonus = RelayBonus.VALUES[ordinal()];
        this.relayTransfer = RelayTransfer.VALUES[ordinal()];
        this.powerFlowerOutput = PowerFlowerOutput.VALUES[ordinal()];
        this.fluidEfficiency = fluidEfficiency;
    }

    /**
     * @return EMC 接口导出流体时,能从"整桶价"里扣掉多少百分比(桶本身的价值不算钱)。
     *         {@link com.projecte.expansion.Config#enableFluidEfficiency} 关掉时固定 100%。
     */
    public int getFluidEfficiencyPercentage() {
        if (!Config.enableFluidEfficiency) return 100;
        int efficiency = fluidEfficiency;
        for (Matter m : VALUES) {
            if (m.level < level) efficiency += m.fluidEfficiency;
        }
        return efficiency;
    }

    /** @return EMC 接口每秒能注入所有者 EMC 的 EMC 上限;MK16 无上限,返回 null。 */
    public BigInteger getEmcLinkEmcLimit() {
        if (this == FINAL) return null;
        return BigDecimal.valueOf(16)
            .pow(level)
            .multiply(BigDecimal.valueOf(Config.emcLinkEMCLimitMultiplier))
            .toBigInteger();
    }

    /** @return EMC 接口每秒能导入/导出的物品个数上限;MK16 无上限,返回 {@link Integer#MAX_VALUE}。 */
    public int getEmcLinkItemLimit() {
        if (this == FINAL) return Integer.MAX_VALUE;
        return BigDecimal.valueOf(2)
            .pow(level - 1)
            .multiply(BigDecimal.valueOf(Config.emcLinkItemLimitMultiplier))
            .intValue();
    }

    /** @return EMC 接口每秒能导出的流体上限(mB);MK16 无上限。一桶 = 1000 mB。 */
    public int getEmcLinkFluidLimit() {
        if (this == FINAL) return Integer.MAX_VALUE;
        return BigDecimal.valueOf(2)
            .pow(level - 1)
            .multiply(BigDecimal.valueOf(1000))
            .multiply(BigDecimal.valueOf(Config.emcLinkFluidLimitMultiplier))
            .intValue();
    }

    /** @return EMC 接口的物品栏槽位数(上游:层级 × 3)。 */
    public int getEmcLinkInventorySize() {
        return level * 3;
    }

    public Matter prev() {
        return VALUES[(ordinal() - 1 + VALUES.length) % VALUES.length];
    }

    public Matter next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    public boolean hasOwnItem() {
        return this != BASIC && this != DARK && this != RED && this != FINAL;
    }

    public boolean hasOwnBlock() {
        return this != BASIC && this != DARK && this != RED && this != FINAL;
    }

    /** 上游的稀有度分组;PINK 在上游同样归入 COMMON。 */
    public EnumRarity getRarity() {
        int o = ordinal();
        if (o <= 2 || o == 4) return EnumRarity.common;
        if (o <= 7) return EnumRarity.uncommon; // MAGENTA 到 BLUE
        if (o <= 11) return EnumRarity.rare; // CYAN 到 YELLOW
        return EnumRarity.epic; // ORANGE 到 FINAL
    }
}
