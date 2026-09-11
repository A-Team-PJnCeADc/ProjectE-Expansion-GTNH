package com.projecte.expansion.registry;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;

import com.projecte.expansion.block.BlockAdvancedAlchemicalChestPX;
import com.projecte.expansion.block.BlockCollectorPX;
import com.projecte.expansion.block.BlockCompactSunPX;
import com.projecte.expansion.block.BlockCondenserMK3PX;
import com.projecte.expansion.block.BlockEMCLinkPX;
import com.projecte.expansion.block.BlockPXBase;
import com.projecte.expansion.block.BlockPowerFlowerPX;
import com.projecte.expansion.block.BlockRelayPX;
import com.projecte.expansion.block.BlockTransmutationInterfacePX;
import com.projecte.expansion.item.ItemBlockPX;
import com.projecte.expansion.item.ItemBlockTransmutationInterfacePX;
import com.projecte.expansion.item.ItemPXSimple;
import com.projecte.expansion.tile.TileEntityAdvancedChestPX;
import com.projecte.expansion.tile.TileEntityCondenserMK3PX;
import com.projecte.expansion.tile.TileEntityPowerFlowerPX;
import com.projecte.expansion.tile.TileEntityTransmutationInterfacePX;
import com.projecte.expansion.util.Fuel;
import com.projecte.expansion.util.Matter;
import com.projecte.expansion.util.Star;

import cpw.mods.fml.common.registry.GameRegistry;

/**
 * 注册本 mod 的所有方块/物品，注册名与创造标签页布局
 * 均与上游 Project Expansion 保持一致。
 */
public final class PXContent {

    private PXContent() {}

    /** DyeColor.values() 的顺序（现代名称，与箱子贴图文件名一致）。 */
    private static final String[] CHEST_COLORS = { "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink",
        "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black" };

    public static void register() {
        // 先初始化标签页持有者（方块物品会在其构造函数中引用它）
        PXCreativeTab.TAB.hashCode();
        registerBlocks();
        registerItems();
        fillCreativeTab();
    }

    private static String tier(Matter m) {
        return m.name()
            .toLowerCase(java.util.Locale.US);
    }

    private static void registerBlocks() {
        // 各等级机器
        for (Matter m : Matter.VALUES) {
            String t = tier(m);
            BlockCollectorPX collector = new BlockCollectorPX(m);
            GameRegistry.registerBlock(collector, ItemBlockPX.class, t + "_collector");
            PXItems.COLLECTORS.put(m, collector);

            BlockRelayPX relay = new BlockRelayPX(m);
            GameRegistry.registerBlock(relay, ItemBlockPX.class, t + "_relay");
            PXItems.RELAYS.put(m, relay);

            BlockPowerFlowerPX flower = new BlockPowerFlowerPX(m);
            GameRegistry.registerBlock(flower, ItemBlockPX.class, t + "_power_flower");
            PXItems.POWER_FLOWERS.put(m, flower);
            PXItems.POWER_FLOWER_ITEMS.put(Item.getItemFromBlock(flower), m);

            BlockEMCLinkPX link = new BlockEMCLinkPX(m);
            GameRegistry.registerBlock(link, ItemBlockPX.class, t + "_emc_link");
            PXItems.EMC_LINKS.put(m, link);
        }
        // 带颜色的物质方块（basic/dark/red/final 没有独立方块，与上游一致）
        for (Matter m : Matter.VALUES) {
            if (!m.hasOwnBlock()) continue;
            String t = tier(m);
            BlockPXBase block = new BlockPXBase(t + "_matter_block", Material.rock, 3.0F, 10.0F, "matter/" + t);
            GameRegistry.registerBlock(block, ItemBlockPX.class, t + "_matter_block");
            PXItems.MATTER_BLOCKS.put(m, block);
        }
        // 带颜色的燃料方块
        for (Fuel f : Fuel.VALUES) {
            if (!f.hasOwnItem()) continue;
            String c = f.name()
                .toLowerCase(java.util.Locale.US);
            BlockPXBase block = new BlockPXBase(c + "_fuel_block", Material.rock, 3.0F, 10.0F, "fuel/" + c);
            GameRegistry.registerBlock(block, ItemBlockPX.class, c + "_fuel_block");
            PXItems.FUEL_BLOCKS.put(f, block);
        }
        // 高级炼金箱（每种染料颜色对应一个方块）
        for (String color : CHEST_COLORS) {
            BlockAdvancedAlchemicalChestPX chest = new BlockAdvancedAlchemicalChestPX(color);
            GameRegistry.registerBlock(chest, ItemBlockPX.class, color + "_advanced_alchemical_chest");
            PXItems.CHESTS.put(color, chest);
            PXItems.CHEST_ITEMS.put(Item.getItemFromBlock(chest), color);
        }
        GameRegistry
            .registerTileEntity(TileEntityAdvancedChestPX.class, "projectexpansiongtnh.advanced_alchemical_chest");
        GameRegistry.registerTileEntity(TileEntityCondenserMK3PX.class, "projectexpansiongtnh.condenser_mk3");
        GameRegistry.registerTileEntity(TileEntityPowerFlowerPX.class, "projectexpansiongtnh.power_flower");
        GameRegistry.registerTileEntity(
            TileEntityTransmutationInterfacePX.class,
            "projectexpansiongtnh.transmutation_interface");
        // 单例
        PXItems.transmutationInterface = new BlockTransmutationInterfacePX();
        GameRegistry.registerBlock(
            PXItems.transmutationInterface,
            ItemBlockTransmutationInterfacePX.class,
            "transmutation_interface");
        PXItems.compactSun = registerBlock(new BlockCompactSunPX(), "compact_sun");
        PXItems.condenserMK3 = registerBlock(new BlockCondenserMK3PX(), "condenser_mk3");
    }

    private static Block registerBlock(Block block, String name) {
        GameRegistry.registerBlock(block, ItemBlockPX.class, name);
        return block;
    }

    private static void registerItems() {
        // 带颜色的物质物品
        for (Matter m : Matter.VALUES) {
            if (!m.hasOwnItem()) continue;
            String c = tier(m);
            Item item = new ItemPXSimple(c + "_matter", "matter/" + c, m.getRarity());
            GameRegistry.registerItem(item, c + "_matter");
            PXItems.MATTER_ITEMS.put(m, item);
        }
        // 带颜色的燃料物品
        for (Fuel f : Fuel.VALUES) {
            if (!f.hasOwnItem()) continue;
            String c = f.name()
                .toLowerCase(java.util.Locale.US);
            Item item = new ItemPXSimple(c + "_fuel", "fuel/" + c, f.getRarity());
            GameRegistry.registerItem(item, c + "_fuel");
            PXItems.FUEL_ITEMS.put(f, item);
        }
        // 压缩收集器（普通物品，使用收集器方块模型渲染）
        for (Matter m : Matter.VALUES) {
            String t = tier(m);
            Item item = new ItemPXSimple(t + "_compressed_collector", "collector/" + t);
            GameRegistry.registerItem(item, t + "_compressed_collector");
            PXItems.COMPRESSED_COLLECTORS.put(m, item);
            PXItems.COMPRESSED_COLLECTOR_BLOCKS.put(item, PXItems.COLLECTORS.get(m));
        }
        // 星：magnum/colossal/gargantuan x ein..omega
        for (Star.StarType type : Star.StarType.values()) {
            String typeDir = type.name()
                .toLowerCase(java.util.Locale.US);
            for (Star s : Star.VALUES) {
                String n = s.name()
                    .toLowerCase(java.util.Locale.US);
                Item item = new ItemPXSimple(typeDir + "_star_" + n, "star/" + typeDir + "/" + n);
                GameRegistry.registerItem(item, typeDir + "_star_" + n);
                PXItems.STARS[type.ordinal()][s.ordinal()] = item;
            }
        }
        // 单例物品
        PXItems.finalStarShard = registerItem(
            new ItemPXSimple("final_star_shard", "star/final_shard", EnumRarity.epic),
            "final_star_shard");
        PXItems.finalStar = registerItem(new ItemPXSimple("final_star", "star/final", EnumRarity.epic), "final_star");
        PXItems.knowledgeSharingBook = registerItem(
            new ItemPXSimple("knowledge_sharing_book", "knowledge_sharing_book"),
            "knowledge_sharing_book");
        PXItems.basicAlchemicalBook = registerItem(
            new ItemPXSimple("basic_alchemical_book", "basic_alchemical_book"),
            "basic_alchemical_book");
        PXItems.advancedAlchemicalBook = registerItem(
            new ItemPXSimple("advanced_alchemical_book", "advanced_alchemical_book"),
            "advanced_alchemical_book");
        PXItems.masterAlchemicalBook = registerItem(
            new ItemPXSimple("master_alchemical_book", "master_alchemical_book"),
            "master_alchemical_book");
        PXItems.arcaneAlchemicalBook = registerItem(
            new ItemPXSimple("arcane_alchemical_book", "arcane_alchemical_book"),
            "arcane_alchemical_book");
        PXItems.arcaneTransmutationTablet = registerItem(
            new ItemPXSimple("arcane_transmutation_tablet", "arcane_transmutation_tablet", EnumRarity.epic),
            "arcane_transmutation_tablet");
    }

    private static Item registerItem(Item item, String name) {
        GameRegistry.registerItem(item, name);
        return item;
    }

    /** 按与上游相同的视觉顺序填充创造标签页。 */
    private static void fillCreativeTab() {
        // 星排在最前（先按类型分组，再按等级）
        for (Star.StarType type : Star.StarType.values()) {
            for (int i = 0; i < Star.VALUES.length; i++) {
                PXCreativeTab.add(PXItems.STARS[type.ordinal()][i]);
            }
        }
        // 先物质物品，再物质方块
        for (Matter m : Matter.VALUES) {
            Item i = PXItems.MATTER_ITEMS.get(m);
            if (i != null) PXCreativeTab.add(i);
        }
        for (Matter m : Matter.VALUES) {
            Block b = PXItems.MATTER_BLOCKS.get(m);
            if (b != null) PXCreativeTab.add(Item.getItemFromBlock(b));
        }
        // 收集器、压缩收集器、力量花、继电器、EMC 接口
        for (Matter m : Matter.VALUES) PXCreativeTab.add(Item.getItemFromBlock(PXItems.COLLECTORS.get(m)));
        for (Matter m : Matter.VALUES) PXCreativeTab.add(PXItems.COMPRESSED_COLLECTORS.get(m));
        for (Matter m : Matter.VALUES) PXCreativeTab.add(Item.getItemFromBlock(PXItems.POWER_FLOWERS.get(m)));
        for (Matter m : Matter.VALUES) PXCreativeTab.add(Item.getItemFromBlock(PXItems.RELAYS.get(m)));
        for (Matter m : Matter.VALUES) PXCreativeTab.add(Item.getItemFromBlock(PXItems.EMC_LINKS.get(m)));
        // 先燃料，再燃料方块
        for (Fuel f : Fuel.VALUES) {
            Item i = PXItems.FUEL_ITEMS.get(f);
            if (i != null) PXCreativeTab.add(i);
        }
        for (Fuel f : Fuel.VALUES) {
            Block b = PXItems.FUEL_BLOCKS.get(f);
            if (b != null) PXCreativeTab.add(Item.getItemFromBlock(b));
        }
        // 高级炼金箱
        for (String color : CHEST_COLORS) PXCreativeTab.add(Item.getItemFromBlock(PXItems.CHESTS.get(color)));
        // 单个物品
        PXCreativeTab.add(PXItems.finalStarShard);
        PXCreativeTab.add(PXItems.finalStar);
        PXCreativeTab.add(Item.getItemFromBlock(PXItems.transmutationInterface));
        PXCreativeTab.add(PXItems.knowledgeSharingBook);
        PXCreativeTab.add(PXItems.basicAlchemicalBook);
        PXCreativeTab.add(PXItems.advancedAlchemicalBook);
        PXCreativeTab.add(PXItems.masterAlchemicalBook);
        PXCreativeTab.add(PXItems.arcaneAlchemicalBook);
        PXCreativeTab.add(Item.getItemFromBlock(PXItems.compactSun));
        PXCreativeTab.add(Item.getItemFromBlock(PXItems.condenserMK3));
        PXCreativeTab.add(PXItems.arcaneTransmutationTablet);
    }
}
