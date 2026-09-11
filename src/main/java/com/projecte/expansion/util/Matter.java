package com.projecte.expansion.util;

import net.minecraft.item.EnumRarity;

/**
 * Project Expansion 的 16 个物质层级。BASIC/DARK/RED 复用 ProjectE 的内容
 * (炼金煤炭 / 暗物质 / 红物质),FINAL 复用终极之星碎片,
 * 与上游的 hasItem/hasBlock 规则保持一致。
 */
public enum Matter {

    BASIC(0),
    DARK(1),
    RED(2),
    MAGENTA(3),
    PINK(4),
    PURPLE(5),
    VIOLET(6),
    BLUE(7),
    CYAN(8),
    GREEN(9),
    LIME(10),
    YELLOW(11),
    ORANGE(12),
    WHITE(13),
    FADING(14),
    FINAL(15);

    public static final Matter[] VALUES = values();
    /** 该层级收集器每秒的基础 EMC 产出(未乘配置(config)倍率)。 */
    public final long collectorOutput;
    /** 该层级继电器每秒发放的 EMC 加成(未乘配置(config)倍率)。 */
    public final long relayBonus;
    /** 该层级继电器每秒可转移的最大 EMC(未乘配置(config)倍率)。 */
    public final long relayTransfer;
    public final int level;

    Matter(int ignored) {
        this.level = ordinal() + 1;
        this.collectorOutput = scaleBase(4L);
        this.relayBonus = scaleBase(1L);
        this.relayTransfer = name().equals("FINAL") ? Long.MAX_VALUE : scaleBase(64L);
    }

    private long scaleBase(long base) {
        try {
            for (int i = 0; i < ordinal(); i++) {
                base = Math.multiplyExact(base, 6L);
            }
            return base;
        } catch (ArithmeticException e) {
            return Long.MAX_VALUE;
        }
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
