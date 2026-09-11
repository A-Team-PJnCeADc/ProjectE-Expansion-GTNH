package com.projecte.expansion.util;

/**
 * Klein 风格的星等级:EIN(1) .. OMEGA(6),每级都有
 * magnum / colossal / gargantuan 三种星(共 18 个物品)。
 */
public enum Star {

    EIN,
    ZWEI,
    DREI,
    VIER,
    SPHERE,
    OMEGA;

    public static final Star[] VALUES = values();

    public String typeDir(StarType type) {
        return type.name()
            .toLowerCase(java.util.Locale.US);
    }

    public enum StarType {
        MAGNUM,
        COLOSSAL,
        GARGANTUAN
    }
}
