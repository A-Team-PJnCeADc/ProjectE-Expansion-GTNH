package com.projecte.expansion.registry;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

import com.projecte.expansion.util.Fuel;
import com.projecte.expansion.util.Matter;
import com.projecte.expansion.util.Star;

/** 存放 PXContent.register() 所创建内容的静态字段。 */
public final class PXItems {

    private PXItems() {}

    public static Item finalStar;
    public static Item finalStarShard;
    public static Item knowledgeSharingBook;
    public static Item arcaneTransmutationTablet;
    public static Item basicAlchemicalBook;
    public static Item advancedAlchemicalBook;
    public static Item masterAlchemicalBook;
    public static Item arcaneAlchemicalBook;

    public static Block transmutationInterface;
    public static Block compactSun;
    public static Block condenserMK3;

    public static final Map<Matter, Item> MATTER_ITEMS = new EnumMap<>(Matter.class);
    public static final Map<Matter, Block> COLLECTORS = new EnumMap<>(Matter.class);
    public static final Map<Matter, Block> RELAYS = new EnumMap<>(Matter.class);
    public static final Map<Matter, Block> POWER_FLOWERS = new EnumMap<>(Matter.class);
    /** 力量花物品,以 item -> 品阶 为键(3D 物品渲染器) */
    public static final Map<Item, Matter> POWER_FLOWER_ITEMS = new HashMap<>();
    public static final Map<Matter, Block> EMC_LINKS = new EnumMap<>(Matter.class);
    public static final Map<Matter, Block> MATTER_BLOCKS = new EnumMap<>(Matter.class);
    public static final Map<Matter, Item> COMPRESSED_COLLECTORS = new EnumMap<>(Matter.class);
    public static final Map<Fuel, Item> FUEL_ITEMS = new EnumMap<>(Fuel.class);
    public static final Map<Fuel, Block> FUEL_BLOCKS = new EnumMap<>(Fuel.class);
    /** stars[starType.ordinal()][star.ordinal()] */
    public static final Item[][] STARS = new Item[Star.StarType.values().length][Star.VALUES.length];
    /** 高级炼金箱子,以染料颜色名称为键 */
    public static final Map<String, Block> CHESTS = new HashMap<>();
    /** 高级炼金箱子物品,以 item -> 染料颜色名称 为键(物品渲染器) */
    public static final Map<Item, String> CHEST_ITEMS = new HashMap<>();
    /** 压缩收集器物品,以 item -> 同品阶的收集器方块 为键(3D 物品渲染器) */
    public static final Map<Item, Block> COMPRESSED_COLLECTOR_BLOCKS = new HashMap<>();

    static String lower(Enum<?> e) {
        return e.name()
            .toLowerCase(Locale.US);
    }
}
