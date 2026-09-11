package com.projecte.expansion.util;

import net.minecraft.item.EnumRarity;

/**
 * 燃料等级。前三种(炼金煤炭、莫比乌斯燃料、永恒燃料)是 ProjectE 的物品;
 * 本 mod 只登记从 MAGENTA 起的彩色燃料,每种都同时登记为物品和燃料方块。
 * 彩色燃料 = 同颜色等级的物质。
 */
public enum Fuel {

    ALCHEMICAL,
    MOBIUS,
    AETERNALIS,
    MAGENTA,
    PINK,
    PURPLE,
    VIOLET,
    BLUE,
    CYAN,
    GREEN,
    LIME,
    YELLOW,
    ORANGE,
    WHITE;

    public static final Fuel[] VALUES = values();

    public boolean hasOwnItem() {
        return ordinal() >= 3;
    }

    public Matter asMatter() {
        if (this == ALCHEMICAL) return Matter.BASIC;
        if (this == MOBIUS) return Matter.DARK;
        if (this == AETERNALIS) return Matter.RED;
        // 彩色燃料与对应颜色等级的物质同名
        return Matter.valueOf(name());
    }

    /** 上游的稀有度分组(与燃料颜色集合对应)。 */
    public EnumRarity getRarity() {
        int o = ordinal();
        if (o <= 2) return EnumRarity.common;
        if (o <= 6) return EnumRarity.uncommon; // MAGENTA..BLUE
        if (o <= 10) return EnumRarity.rare; // CYAN..YELLOW
        return EnumRarity.epic; // ORANGE, WHITE
    }
}
