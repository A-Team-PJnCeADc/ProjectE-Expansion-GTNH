package com.projecte.expansion.registry;

// 由 tools/gen_recipes.py 从上游 ProjectExpansion 配方 JSON 生成
// (127 个无序配方 + 121 个有序配方)。请勿手改。

import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import com.projecte.expansion.util.Fuel;
import com.projecte.expansion.util.Matter;
import com.projecte.expansion.util.Star;

import cpw.mods.fml.common.registry.GameRegistry;
import moze_intel.projecte.gameObjs.ObjHandler;

/** 全部合成配方,按上游数据文件 1:1 移植。 */
public final class PXRecipes {

    private PXRecipes() {}

    public static void register() {
        // 本 mod 的箱子,对应标签 'projectexpansion:advanced_alchemical_chest' / 'c:chests'
        for (Block chest : PXItems.CHESTS.values()) {
            OreDictionary.registerOre("pxAdvancedChest", new ItemStack(chest));
            OreDictionary.registerOre("pxChest", new ItemStack(chest));
        }
        OreDictionary.registerOre("pxChest", new ItemStack(Blocks.chest));
        OreDictionary.registerOre("pxChest", new ItemStack(Blocks.trapped_chest));
        OreDictionary.registerOre("pxChest", new ItemStack(PXItems.condenserMK3));
        // 上游配方所用 c: 标签的兜底矿物词典(有些整合包从不登记它们)
        OreDictionary.registerOre("obsidian", new ItemStack(Blocks.obsidian));
        OreDictionary.registerOre("blockGlass", new ItemStack(Blocks.glass));
        OreDictionary.registerOre("craftingTableWood", new ItemStack(Blocks.crafting_table));

        addShaped(
            px("advanced_alchemical_book"),
            "EOE",
            "OBO",
            "EOE",
            'B',
            px("basic_alchemical_book"),
            'E',
            px("magnum_star_ein"),
            'O',
            px("magnum_star_omega"));
        addShaped(
            px("black_advanced_alchemical_chest"),
            "DLD",
            "MBM",
            "HLH",
            'B',
            pe("black_alchemical_bag"),
            'D',
            pe("dark_matter"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"));
        addShaped(
            px("blue_advanced_alchemical_chest"),
            "DLD",
            "MBM",
            "HLH",
            'B',
            pe("blue_alchemical_bag"),
            'D',
            pe("dark_matter"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"));
        addShaped(
            px("brown_advanced_alchemical_chest"),
            "DLD",
            "MBM",
            "HLH",
            'B',
            pe("brown_alchemical_bag"),
            'D',
            pe("dark_matter"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"));
        addShaped(
            px("cyan_advanced_alchemical_chest"),
            "DLD",
            "MBM",
            "HLH",
            'B',
            pe("cyan_alchemical_bag"),
            'D',
            pe("dark_matter"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"));
        addShaped(
            px("gray_advanced_alchemical_chest"),
            "DLD",
            "MBM",
            "HLH",
            'B',
            pe("gray_alchemical_bag"),
            'D',
            pe("dark_matter"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"));
        addShaped(
            px("green_advanced_alchemical_chest"),
            "DLD",
            "MBM",
            "HLH",
            'B',
            pe("green_alchemical_bag"),
            'D',
            pe("dark_matter"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"));
        addShaped(
            px("light_blue_advanced_alchemical_chest"),
            "DLD",
            "MBM",
            "HLH",
            'B',
            pe("light_blue_alchemical_bag"),
            'D',
            pe("dark_matter"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"));
        addShaped(
            px("light_gray_advanced_alchemical_chest"),
            "DLD",
            "MBM",
            "HLH",
            'B',
            pe("light_gray_alchemical_bag"),
            'D',
            pe("dark_matter"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"));
        addShaped(
            px("lime_advanced_alchemical_chest"),
            "DLD",
            "MBM",
            "HLH",
            'B',
            pe("lime_alchemical_bag"),
            'D',
            pe("dark_matter"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"));
        addShaped(
            px("magenta_advanced_alchemical_chest"),
            "DLD",
            "MBM",
            "HLH",
            'B',
            pe("magenta_alchemical_bag"),
            'D',
            pe("dark_matter"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"));
        addShaped(
            px("orange_advanced_alchemical_chest"),
            "DLD",
            "MBM",
            "HLH",
            'B',
            pe("orange_alchemical_bag"),
            'D',
            pe("dark_matter"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"));
        addShaped(
            px("pink_advanced_alchemical_chest"),
            "DLD",
            "MBM",
            "HLH",
            'B',
            pe("pink_alchemical_bag"),
            'D',
            pe("dark_matter"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"));
        addShaped(
            px("purple_advanced_alchemical_chest"),
            "DLD",
            "MBM",
            "HLH",
            'B',
            pe("purple_alchemical_bag"),
            'D',
            pe("dark_matter"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"));
        addShapeless(px("black_advanced_alchemical_chest"), "pxAdvancedChest", mc("black_dye"));
        addShapeless(px("blue_advanced_alchemical_chest"), "pxAdvancedChest", mc("blue_dye"));
        addShapeless(px("brown_advanced_alchemical_chest"), "pxAdvancedChest", mc("brown_dye"));
        addShapeless(px("cyan_advanced_alchemical_chest"), "pxAdvancedChest", mc("cyan_dye"));
        addShapeless(px("gray_advanced_alchemical_chest"), "pxAdvancedChest", mc("gray_dye"));
        addShapeless(px("green_advanced_alchemical_chest"), "pxAdvancedChest", mc("green_dye"));
        addShapeless(px("light_blue_advanced_alchemical_chest"), "pxAdvancedChest", mc("light_blue_dye"));
        addShapeless(px("light_gray_advanced_alchemical_chest"), "pxAdvancedChest", mc("light_gray_dye"));
        addShapeless(px("lime_advanced_alchemical_chest"), "pxAdvancedChest", mc("lime_dye"));
        addShapeless(px("magenta_advanced_alchemical_chest"), "pxAdvancedChest", mc("magenta_dye"));
        addShapeless(px("orange_advanced_alchemical_chest"), "pxAdvancedChest", mc("orange_dye"));
        addShapeless(px("pink_advanced_alchemical_chest"), "pxAdvancedChest", mc("pink_dye"));
        addShapeless(px("purple_advanced_alchemical_chest"), "pxAdvancedChest", mc("purple_dye"));
        addShapeless(px("red_advanced_alchemical_chest"), "pxAdvancedChest", mc("red_dye"));
        addShapeless(px("white_advanced_alchemical_chest"), "pxAdvancedChest", mc("white_dye"));
        addShapeless(px("yellow_advanced_alchemical_chest"), "pxAdvancedChest", mc("yellow_dye"));
        addShaped(
            px("red_advanced_alchemical_chest"),
            "DLD",
            "MBM",
            "HLH",
            'B',
            pe("red_alchemical_bag"),
            'D',
            pe("dark_matter"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"));
        addShaped(
            px("white_advanced_alchemical_chest"),
            "DLD",
            "MBM",
            "HLH",
            'B',
            pe("white_alchemical_bag"),
            'D',
            pe("dark_matter"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"));
        addShaped(
            px("yellow_advanced_alchemical_chest"),
            "DLD",
            "MBM",
            "HLH",
            'B',
            pe("yellow_alchemical_bag"),
            'D',
            pe("dark_matter"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"));
        addShaped(
            px("arcane_alchemical_book"),
            "SFS",
            "FBF",
            "SFS",
            'B',
            px("master_alchemical_book"),
            'F',
            px("final_star"),
            'S',
            px("final_star_shard"));
        addShaped(
            px("arcane_transmutation_tablet"),
            "TWT",
            "MSM",
            "TCT",
            'C',
            "pxChest",
            'M',
            px("magenta_matter"),
            'S',
            px("magnum_star_ein"),
            'T',
            pe("transmutation_tablet"),
            'W',
            "craftingTableWood");
        addShaped(
            px("basic_alchemical_book"),
            "HRH",
            "EBP",
            "HRH",
            'B',
            mc("book"),
            'E',
            mc("ender_pearl"),
            'H',
            pe("high_covalence_dust"),
            'P',
            pe("philosophers_stone"),
            'R',
            pe("red_matter"));
        addShaped(
            px("basic_collector"),
            "GSG",
            "GAG",
            "GFG",
            'A',
            pe("aeternalis_fuel_block"),
            'F',
            mc("furnace"),
            'G',
            mc("glowstone"),
            'S',
            "blockGlass");
        addShapeless(px("blue_collector"), px("violet_collector"), px("blue_matter"));
        addShaped(px("basic_compressed_collector"), "CCC", "CCC", "CCC", 'C', px("basic_collector"));
        addShaped(px("blue_compressed_collector"), "CCC", "CCC", "CCC", 'C', px("blue_collector"));
        addShaped(px("cyan_compressed_collector"), "CCC", "CCC", "CCC", 'C', px("cyan_collector"));
        addShaped(px("dark_compressed_collector"), "CCC", "CCC", "CCC", 'C', px("dark_collector"));
        addShaped(px("fading_compressed_collector"), "CCC", "CCC", "CCC", 'C', px("fading_collector"));
        addShaped(px("final_compressed_collector"), "CCC", "CCC", "CCC", 'C', px("final_collector"));
        addShaped(px("green_compressed_collector"), "CCC", "CCC", "CCC", 'C', px("green_collector"));
        addShaped(px("lime_compressed_collector"), "CCC", "CCC", "CCC", 'C', px("lime_collector"));
        addShaped(px("magenta_compressed_collector"), "CCC", "CCC", "CCC", 'C', px("magenta_collector"));
        addShaped(px("orange_compressed_collector"), "CCC", "CCC", "CCC", 'C', px("orange_collector"));
        addShaped(px("pink_compressed_collector"), "CCC", "CCC", "CCC", 'C', px("pink_collector"));
        addShaped(px("purple_compressed_collector"), "CCC", "CCC", "CCC", 'C', px("purple_collector"));
        addShaped(px("red_compressed_collector"), "CCC", "CCC", "CCC", 'C', px("red_collector"));
        addShaped(px("violet_compressed_collector"), "CCC", "CCC", "CCC", 'C', px("violet_collector"));
        addShaped(px("white_compressed_collector"), "CCC", "CCC", "CCC", 'C', px("white_collector"));
        addShaped(px("yellow_compressed_collector"), "CCC", "CCC", "CCC", 'C', px("yellow_collector"));
        addShapeless(px("cyan_collector"), px("blue_collector"), px("cyan_matter"));
        addShapeless(px("dark_collector"), px("basic_collector"), pe("dark_matter"));
        addShapeless(px("fading_collector"), px("white_collector"), px("fading_matter"));
        addShapeless(px("final_collector"), px("fading_collector"), px("final_star_shard"));
        addShapeless(px("green_collector"), px("cyan_collector"), px("green_matter"));
        addShapeless(px("lime_collector"), px("green_collector"), px("lime_matter"));
        addShapeless(px("magenta_collector"), px("red_collector"), px("magenta_matter"));
        addShapeless(px("basic_collector"), pe("collector_mk1"));
        addShapeless(px("dark_collector"), pe("collector_mk2"));
        addShapeless(px("red_collector"), pe("collector_mk3"));
        addShapeless(px("orange_collector"), px("yellow_collector"), px("orange_matter"));
        addShapeless(px("pink_collector"), px("magenta_collector"), px("pink_matter"));
        addShapeless(px("purple_collector"), px("pink_collector"), px("purple_matter"));
        addShapeless(px("red_collector"), px("dark_collector"), pe("red_matter"));
        addShapeless(px("violet_collector"), px("purple_collector"), px("violet_matter"));
        addShapeless(px("white_collector"), px("orange_collector"), px("white_matter"));
        addShapeless(px("yellow_collector"), px("lime_collector"), px("yellow_matter"));
        addShaped(
            px("compact_sun"),
            "HSH",
            "SYS",
            "HSH",
            'H',
            px("final_star_shard"),
            'S',
            px("final_star"),
            'Y',
            px("yellow_matter"));
        addShaped(
            px("compact_sun"),
            "SHS",
            "HYH",
            "SHS",
            'H',
            px("final_star_shard"),
            'S',
            px("final_star"),
            'Y',
            px("yellow_matter"));
        addShaped(px("condenser_mk3"), "MCM", "CCC", "MCM", 'C', pe("condenser_mk2"), 'M', px("magenta_matter_block"));
        addShaped(
            px("basic_emc_link"),
            "LMH",
            "TCT",
            "HML",
            'C',
            pe("condenser_mk1"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"),
            'T',
            pe("transmutation_tablet"));
        addShaped(
            px("blue_emc_link"),
            "LMH",
            "TCT",
            "HML",
            'C',
            px("violet_emc_link"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"),
            'T',
            px("blue_matter"));
        addShaped(
            px("cyan_emc_link"),
            "LMH",
            "TCT",
            "HML",
            'C',
            px("blue_emc_link"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"),
            'T',
            px("cyan_matter"));
        addShaped(
            px("dark_emc_link"),
            "LMH",
            "TCT",
            "HML",
            'C',
            px("basic_emc_link"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"),
            'T',
            pe("dark_matter"));
        addShaped(
            px("fading_emc_link"),
            "LMH",
            "TCT",
            "HML",
            'C',
            px("white_emc_link"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"),
            'T',
            px("fading_matter"));
        addShaped(
            px("final_emc_link"),
            "LMH",
            "TCT",
            "HML",
            'C',
            px("fading_emc_link"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"),
            'T',
            px("final_star_shard"));
        addShaped(
            px("green_emc_link"),
            "LMH",
            "TCT",
            "HML",
            'C',
            px("cyan_emc_link"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"),
            'T',
            px("green_matter"));
        addShaped(
            px("lime_emc_link"),
            "LMH",
            "TCT",
            "HML",
            'C',
            px("green_emc_link"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"),
            'T',
            px("lime_matter"));
        addShaped(
            px("magenta_emc_link"),
            "LMH",
            "TCT",
            "HML",
            'C',
            px("red_emc_link"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"),
            'T',
            px("magenta_matter"));
        addShaped(
            px("orange_emc_link"),
            "LMH",
            "TCT",
            "HML",
            'C',
            px("yellow_emc_link"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"),
            'T',
            px("orange_matter"));
        addShaped(
            px("pink_emc_link"),
            "LMH",
            "TCT",
            "HML",
            'C',
            px("magenta_emc_link"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"),
            'T',
            px("pink_matter"));
        addShaped(
            px("purple_emc_link"),
            "LMH",
            "TCT",
            "HML",
            'C',
            px("pink_emc_link"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"),
            'T',
            px("purple_matter"));
        addShaped(
            px("red_emc_link"),
            "LMH",
            "TCT",
            "HML",
            'C',
            px("dark_emc_link"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"),
            'T',
            pe("red_matter"));
        addShaped(
            px("violet_emc_link"),
            "LMH",
            "TCT",
            "HML",
            'C',
            px("purple_emc_link"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"),
            'T',
            px("violet_matter"));
        addShaped(
            px("white_emc_link"),
            "LMH",
            "TCT",
            "HML",
            'C',
            px("orange_emc_link"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"),
            'T',
            px("white_matter"));
        addShaped(
            px("yellow_emc_link"),
            "LMH",
            "TCT",
            "HML",
            'C',
            px("lime_emc_link"),
            'H',
            pe("high_covalence_dust"),
            'L',
            pe("low_covalence_dust"),
            'M',
            pe("medium_covalence_dust"),
            'T',
            px("yellow_matter"));
        addShapeless(
            px("blue_fuel_block"),
            px("blue_fuel"),
            px("blue_fuel"),
            px("blue_fuel"),
            px("blue_fuel"),
            px("blue_fuel"),
            px("blue_fuel"),
            px("blue_fuel"),
            px("blue_fuel"),
            px("blue_fuel"));
        addShapeless(st(px("blue_fuel"), 9), px("blue_fuel_block"));
        addShapeless(
            px("cyan_fuel_block"),
            px("cyan_fuel"),
            px("cyan_fuel"),
            px("cyan_fuel"),
            px("cyan_fuel"),
            px("cyan_fuel"),
            px("cyan_fuel"),
            px("cyan_fuel"),
            px("cyan_fuel"),
            px("cyan_fuel"));
        addShapeless(st(px("cyan_fuel"), 9), px("cyan_fuel_block"));
        addShapeless(
            px("green_fuel_block"),
            px("green_fuel"),
            px("green_fuel"),
            px("green_fuel"),
            px("green_fuel"),
            px("green_fuel"),
            px("green_fuel"),
            px("green_fuel"),
            px("green_fuel"),
            px("green_fuel"));
        addShapeless(st(px("green_fuel"), 9), px("green_fuel_block"));
        addShapeless(
            px("lime_fuel_block"),
            px("lime_fuel"),
            px("lime_fuel"),
            px("lime_fuel"),
            px("lime_fuel"),
            px("lime_fuel"),
            px("lime_fuel"),
            px("lime_fuel"),
            px("lime_fuel"),
            px("lime_fuel"));
        addShapeless(st(px("lime_fuel"), 9), px("lime_fuel_block"));
        addShapeless(
            px("magenta_fuel_block"),
            px("magenta_fuel"),
            px("magenta_fuel"),
            px("magenta_fuel"),
            px("magenta_fuel"),
            px("magenta_fuel"),
            px("magenta_fuel"),
            px("magenta_fuel"),
            px("magenta_fuel"),
            px("magenta_fuel"));
        addShapeless(st(px("magenta_fuel"), 9), px("magenta_fuel_block"));
        addShapeless(
            px("orange_fuel_block"),
            px("orange_fuel"),
            px("orange_fuel"),
            px("orange_fuel"),
            px("orange_fuel"),
            px("orange_fuel"),
            px("orange_fuel"),
            px("orange_fuel"),
            px("orange_fuel"),
            px("orange_fuel"));
        addShapeless(st(px("orange_fuel"), 9), px("orange_fuel_block"));
        addShapeless(
            px("pink_fuel_block"),
            px("pink_fuel"),
            px("pink_fuel"),
            px("pink_fuel"),
            px("pink_fuel"),
            px("pink_fuel"),
            px("pink_fuel"),
            px("pink_fuel"),
            px("pink_fuel"),
            px("pink_fuel"));
        addShapeless(st(px("pink_fuel"), 9), px("pink_fuel_block"));
        addShapeless(
            px("purple_fuel_block"),
            px("purple_fuel"),
            px("purple_fuel"),
            px("purple_fuel"),
            px("purple_fuel"),
            px("purple_fuel"),
            px("purple_fuel"),
            px("purple_fuel"),
            px("purple_fuel"),
            px("purple_fuel"));
        addShapeless(st(px("purple_fuel"), 9), px("purple_fuel_block"));
        addShapeless(
            px("violet_fuel_block"),
            px("violet_fuel"),
            px("violet_fuel"),
            px("violet_fuel"),
            px("violet_fuel"),
            px("violet_fuel"),
            px("violet_fuel"),
            px("violet_fuel"),
            px("violet_fuel"),
            px("violet_fuel"));
        addShapeless(st(px("violet_fuel"), 9), px("violet_fuel_block"));
        addShapeless(
            px("white_fuel_block"),
            px("white_fuel"),
            px("white_fuel"),
            px("white_fuel"),
            px("white_fuel"),
            px("white_fuel"),
            px("white_fuel"),
            px("white_fuel"),
            px("white_fuel"),
            px("white_fuel"));
        addShapeless(st(px("white_fuel"), 9), px("white_fuel_block"));
        addShapeless(
            px("yellow_fuel_block"),
            px("yellow_fuel"),
            px("yellow_fuel"),
            px("yellow_fuel"),
            px("yellow_fuel"),
            px("yellow_fuel"),
            px("yellow_fuel"),
            px("yellow_fuel"),
            px("yellow_fuel"),
            px("yellow_fuel"));
        addShapeless(st(px("yellow_fuel"), 9), px("yellow_fuel_block"));
        addShapeless(
            px("blue_fuel"),
            pe("philosophers_stone"),
            px("violet_fuel"),
            px("violet_fuel"),
            px("violet_fuel"),
            px("violet_fuel"));
        addShapeless(
            px("cyan_fuel"),
            pe("philosophers_stone"),
            px("blue_fuel"),
            px("blue_fuel"),
            px("blue_fuel"),
            px("blue_fuel"));
        addShapeless(
            px("green_fuel"),
            pe("philosophers_stone"),
            px("cyan_fuel"),
            px("cyan_fuel"),
            px("cyan_fuel"),
            px("cyan_fuel"));
        addShapeless(
            px("lime_fuel"),
            pe("philosophers_stone"),
            px("green_fuel"),
            px("green_fuel"),
            px("green_fuel"),
            px("green_fuel"));
        addShapeless(
            px("magenta_fuel"),
            pe("philosophers_stone"),
            pe("aeternalis_fuel"),
            pe("aeternalis_fuel"),
            pe("aeternalis_fuel"),
            pe("aeternalis_fuel"));
        addShapeless(
            px("orange_fuel"),
            pe("philosophers_stone"),
            px("yellow_fuel"),
            px("yellow_fuel"),
            px("yellow_fuel"),
            px("yellow_fuel"));
        addShapeless(
            px("pink_fuel"),
            pe("philosophers_stone"),
            px("magenta_fuel"),
            px("magenta_fuel"),
            px("magenta_fuel"),
            px("magenta_fuel"));
        addShapeless(
            px("purple_fuel"),
            pe("philosophers_stone"),
            px("pink_fuel"),
            px("pink_fuel"),
            px("pink_fuel"),
            px("pink_fuel"));
        addShapeless(
            px("violet_fuel"),
            pe("philosophers_stone"),
            px("purple_fuel"),
            px("purple_fuel"),
            px("purple_fuel"),
            px("purple_fuel"));
        addShapeless(
            px("white_fuel"),
            pe("philosophers_stone"),
            px("orange_fuel"),
            px("orange_fuel"),
            px("orange_fuel"),
            px("orange_fuel"));
        addShapeless(
            px("yellow_fuel"),
            pe("philosophers_stone"),
            px("lime_fuel"),
            px("lime_fuel"),
            px("lime_fuel"),
            px("lime_fuel"));
        addShaped(
            px("knowledge_sharing_book"),
            "VNV",
            "VBV",
            "VNV",
            'B',
            mc("book"),
            'N',
            mc("nether_star"),
            'V',
            px("violet_matter"));
        addShaped(
            px("master_alchemical_book"),
            "EOE",
            "OBO",
            "EOE",
            'B',
            px("advanced_alchemical_book"),
            'E',
            px("colossal_star_ein"),
            'O',
            px("colossal_star_omega"));
        addShapeless(
            px("blue_matter_block"),
            px("blue_matter"),
            px("blue_matter"),
            px("blue_matter"),
            px("blue_matter"));
        addShapeless(st(px("blue_matter"), 4), px("blue_matter_block"));
        addShapeless(
            px("cyan_matter_block"),
            px("cyan_matter"),
            px("cyan_matter"),
            px("cyan_matter"),
            px("cyan_matter"));
        addShapeless(st(px("cyan_matter"), 4), px("cyan_matter_block"));
        addShapeless(
            px("fading_matter_block"),
            px("fading_matter"),
            px("fading_matter"),
            px("fading_matter"),
            px("fading_matter"));
        addShapeless(st(px("fading_matter"), 4), px("fading_matter_block"));
        addShapeless(
            px("green_matter_block"),
            px("green_matter"),
            px("green_matter"),
            px("green_matter"),
            px("green_matter"));
        addShapeless(st(px("green_matter"), 4), px("green_matter_block"));
        addShapeless(
            px("lime_matter_block"),
            px("lime_matter"),
            px("lime_matter"),
            px("lime_matter"),
            px("lime_matter"));
        addShapeless(st(px("lime_matter"), 4), px("lime_matter_block"));
        addShapeless(
            px("magenta_matter_block"),
            px("magenta_matter"),
            px("magenta_matter"),
            px("magenta_matter"),
            px("magenta_matter"));
        addShapeless(st(px("magenta_matter"), 4), px("magenta_matter_block"));
        addShapeless(
            px("orange_matter_block"),
            px("orange_matter"),
            px("orange_matter"),
            px("orange_matter"),
            px("orange_matter"));
        addShapeless(st(px("orange_matter"), 4), px("orange_matter_block"));
        addShapeless(
            px("pink_matter_block"),
            px("pink_matter"),
            px("pink_matter"),
            px("pink_matter"),
            px("pink_matter"));
        addShapeless(st(px("pink_matter"), 4), px("pink_matter_block"));
        addShapeless(
            px("purple_matter_block"),
            px("purple_matter"),
            px("purple_matter"),
            px("purple_matter"),
            px("purple_matter"));
        addShapeless(st(px("purple_matter"), 4), px("purple_matter_block"));
        addShapeless(
            px("violet_matter_block"),
            px("violet_matter"),
            px("violet_matter"),
            px("violet_matter"),
            px("violet_matter"));
        addShapeless(st(px("violet_matter"), 4), px("violet_matter_block"));
        addShapeless(
            px("white_matter_block"),
            px("white_matter"),
            px("white_matter"),
            px("white_matter"),
            px("white_matter"));
        addShapeless(st(px("white_matter"), 4), px("white_matter_block"));
        addShapeless(
            px("yellow_matter_block"),
            px("yellow_matter"),
            px("yellow_matter"),
            px("yellow_matter"),
            px("yellow_matter"));
        addShapeless(st(px("yellow_matter"), 4), px("yellow_matter_block"));
        addShaped(px("blue_matter"), "FMF", "FMF", "FMF", 'F', px("violet_fuel"), 'M', px("violet_matter"));
        addShaped(px("blue_matter"), "FFF", "MMM", "FFF", 'F', px("violet_fuel"), 'M', px("violet_matter"));
        addShaped(px("cyan_matter"), "FMF", "FMF", "FMF", 'F', px("blue_fuel"), 'M', px("blue_matter"));
        addShaped(px("cyan_matter"), "FFF", "MMM", "FFF", 'F', px("blue_fuel"), 'M', px("blue_matter"));
        addShaped(px("fading_matter"), "FMF", "FMF", "FMF", 'F', px("white_fuel"), 'M', px("white_matter"));
        addShaped(px("fading_matter"), "FFF", "MMM", "FFF", 'F', px("white_fuel"), 'M', px("white_matter"));
        addShaped(px("green_matter"), "FMF", "FMF", "FMF", 'F', px("cyan_fuel"), 'M', px("cyan_matter"));
        addShaped(px("green_matter"), "FFF", "MMM", "FFF", 'F', px("cyan_fuel"), 'M', px("cyan_matter"));
        addShaped(px("lime_matter"), "FMF", "FMF", "FMF", 'F', px("green_fuel"), 'M', px("green_matter"));
        addShaped(px("lime_matter"), "FFF", "MMM", "FFF", 'F', px("green_fuel"), 'M', px("green_matter"));
        addShaped(px("magenta_matter"), "FMF", "FMF", "FMF", 'F', pe("aeternalis_fuel"), 'M', pe("red_matter"));
        addShaped(px("magenta_matter"), "FFF", "MMM", "FFF", 'F', pe("aeternalis_fuel"), 'M', pe("red_matter"));
        addShaped(px("orange_matter"), "FMF", "FMF", "FMF", 'F', px("yellow_fuel"), 'M', px("yellow_matter"));
        addShaped(px("orange_matter"), "FFF", "MMM", "FFF", 'F', px("yellow_fuel"), 'M', px("yellow_matter"));
        addShaped(px("pink_matter"), "FMF", "FMF", "FMF", 'F', px("magenta_fuel"), 'M', px("magenta_matter"));
        addShaped(px("pink_matter"), "FFF", "MMM", "FFF", 'F', px("magenta_fuel"), 'M', px("magenta_matter"));
        addShaped(px("purple_matter"), "FMF", "FMF", "FMF", 'F', px("pink_fuel"), 'M', px("pink_matter"));
        addShaped(px("purple_matter"), "FFF", "MMM", "FFF", 'F', px("pink_fuel"), 'M', px("pink_matter"));
        addShaped(px("violet_matter"), "FMF", "FMF", "FMF", 'F', px("purple_fuel"), 'M', px("purple_matter"));
        addShaped(px("violet_matter"), "FFF", "MMM", "FFF", 'F', px("purple_fuel"), 'M', px("purple_matter"));
        addShaped(px("white_matter"), "FMF", "FMF", "FMF", 'F', px("orange_fuel"), 'M', px("orange_matter"));
        addShaped(px("white_matter"), "FFF", "MMM", "FFF", 'F', px("orange_fuel"), 'M', px("orange_matter"));
        addShaped(px("yellow_matter"), "FMF", "FMF", "FMF", 'F', px("lime_fuel"), 'M', px("lime_matter"));
        addShaped(px("yellow_matter"), "FFF", "MMM", "FFF", 'F', px("lime_fuel"), 'M', px("lime_matter"));
        addShaped(
            px("basic_power_flower"),
            "CLC",
            "RRR",
            "RRR",
            'C',
            px("basic_compressed_collector"),
            'L',
            px("basic_emc_link"),
            'R',
            px("basic_relay"));
        addShaped(
            px("blue_power_flower"),
            "CLC",
            "RRR",
            "RRR",
            'C',
            px("blue_compressed_collector"),
            'L',
            px("blue_emc_link"),
            'R',
            px("blue_relay"));
        addShaped(
            px("blue_power_flower"),
            "CLC",
            "RPR",
            "RRR",
            'C',
            px("blue_compressed_collector"),
            'L',
            px("blue_emc_link"),
            'P',
            px("violet_power_flower"),
            'R',
            px("blue_relay"));
        addShaped(
            px("cyan_power_flower"),
            "CLC",
            "RRR",
            "RRR",
            'C',
            px("cyan_compressed_collector"),
            'L',
            px("cyan_emc_link"),
            'R',
            px("cyan_relay"));
        addShaped(
            px("cyan_power_flower"),
            "CLC",
            "RPR",
            "RRR",
            'C',
            px("cyan_compressed_collector"),
            'L',
            px("cyan_emc_link"),
            'P',
            px("blue_power_flower"),
            'R',
            px("cyan_relay"));
        addShaped(
            px("dark_power_flower"),
            "CLC",
            "RRR",
            "RRR",
            'C',
            px("dark_compressed_collector"),
            'L',
            px("dark_emc_link"),
            'R',
            px("dark_relay"));
        addShaped(
            px("dark_power_flower"),
            "CLC",
            "RPR",
            "RRR",
            'C',
            px("dark_compressed_collector"),
            'L',
            px("dark_emc_link"),
            'P',
            px("basic_power_flower"),
            'R',
            px("dark_relay"));
        addShaped(
            px("fading_power_flower"),
            "CLC",
            "RRR",
            "RRR",
            'C',
            px("fading_compressed_collector"),
            'L',
            px("fading_emc_link"),
            'R',
            px("fading_relay"));
        addShaped(
            px("fading_power_flower"),
            "CLC",
            "RPR",
            "RRR",
            'C',
            px("fading_compressed_collector"),
            'L',
            px("fading_emc_link"),
            'P',
            px("white_power_flower"),
            'R',
            px("fading_relay"));
        addShaped(
            px("final_power_flower"),
            "CLC",
            "RRR",
            "RRR",
            'C',
            px("final_compressed_collector"),
            'L',
            px("final_emc_link"),
            'R',
            px("final_relay"));
        addShaped(
            px("final_power_flower"),
            "CLC",
            "RPR",
            "RRR",
            'C',
            px("final_compressed_collector"),
            'L',
            px("final_emc_link"),
            'P',
            px("fading_power_flower"),
            'R',
            px("final_relay"));
        addShaped(
            px("green_power_flower"),
            "CLC",
            "RRR",
            "RRR",
            'C',
            px("green_compressed_collector"),
            'L',
            px("green_emc_link"),
            'R',
            px("green_relay"));
        addShaped(
            px("green_power_flower"),
            "CLC",
            "RPR",
            "RRR",
            'C',
            px("green_compressed_collector"),
            'L',
            px("green_emc_link"),
            'P',
            px("cyan_power_flower"),
            'R',
            px("green_relay"));
        addShaped(
            px("lime_power_flower"),
            "CLC",
            "RRR",
            "RRR",
            'C',
            px("lime_compressed_collector"),
            'L',
            px("lime_emc_link"),
            'R',
            px("lime_relay"));
        addShaped(
            px("lime_power_flower"),
            "CLC",
            "RPR",
            "RRR",
            'C',
            px("lime_compressed_collector"),
            'L',
            px("lime_emc_link"),
            'P',
            px("green_power_flower"),
            'R',
            px("lime_relay"));
        addShaped(
            px("magenta_power_flower"),
            "CLC",
            "RRR",
            "RRR",
            'C',
            px("magenta_compressed_collector"),
            'L',
            px("magenta_emc_link"),
            'R',
            px("magenta_relay"));
        addShaped(
            px("magenta_power_flower"),
            "CLC",
            "RPR",
            "RRR",
            'C',
            px("magenta_compressed_collector"),
            'L',
            px("magenta_emc_link"),
            'P',
            px("red_power_flower"),
            'R',
            px("magenta_relay"));
        addShaped(
            px("orange_power_flower"),
            "CLC",
            "RRR",
            "RRR",
            'C',
            px("orange_compressed_collector"),
            'L',
            px("orange_emc_link"),
            'R',
            px("orange_relay"));
        addShaped(
            px("orange_power_flower"),
            "CLC",
            "RPR",
            "RRR",
            'C',
            px("orange_compressed_collector"),
            'L',
            px("orange_emc_link"),
            'P',
            px("yellow_power_flower"),
            'R',
            px("orange_relay"));
        addShaped(
            px("pink_power_flower"),
            "CLC",
            "RRR",
            "RRR",
            'C',
            px("pink_compressed_collector"),
            'L',
            px("pink_emc_link"),
            'R',
            px("pink_relay"));
        addShaped(
            px("pink_power_flower"),
            "CLC",
            "RPR",
            "RRR",
            'C',
            px("pink_compressed_collector"),
            'L',
            px("pink_emc_link"),
            'P',
            px("magenta_power_flower"),
            'R',
            px("pink_relay"));
        addShaped(
            px("purple_power_flower"),
            "CLC",
            "RRR",
            "RRR",
            'C',
            px("purple_compressed_collector"),
            'L',
            px("purple_emc_link"),
            'R',
            px("purple_relay"));
        addShaped(
            px("purple_power_flower"),
            "CLC",
            "RPR",
            "RRR",
            'C',
            px("purple_compressed_collector"),
            'L',
            px("purple_emc_link"),
            'P',
            px("pink_power_flower"),
            'R',
            px("purple_relay"));
        addShaped(
            px("red_power_flower"),
            "CLC",
            "RRR",
            "RRR",
            'C',
            px("red_compressed_collector"),
            'L',
            px("red_emc_link"),
            'R',
            px("red_relay"));
        addShaped(
            px("red_power_flower"),
            "CLC",
            "RPR",
            "RRR",
            'C',
            px("red_compressed_collector"),
            'L',
            px("red_emc_link"),
            'P',
            px("dark_power_flower"),
            'R',
            px("red_relay"));
        addShaped(
            px("violet_power_flower"),
            "CLC",
            "RRR",
            "RRR",
            'C',
            px("violet_compressed_collector"),
            'L',
            px("violet_emc_link"),
            'R',
            px("violet_relay"));
        addShaped(
            px("violet_power_flower"),
            "CLC",
            "RPR",
            "RRR",
            'C',
            px("violet_compressed_collector"),
            'L',
            px("violet_emc_link"),
            'P',
            px("purple_power_flower"),
            'R',
            px("violet_relay"));
        addShaped(
            px("white_power_flower"),
            "CLC",
            "RRR",
            "RRR",
            'C',
            px("white_compressed_collector"),
            'L',
            px("white_emc_link"),
            'R',
            px("white_relay"));
        addShaped(
            px("white_power_flower"),
            "CLC",
            "RPR",
            "RRR",
            'C',
            px("white_compressed_collector"),
            'L',
            px("white_emc_link"),
            'P',
            px("orange_power_flower"),
            'R',
            px("white_relay"));
        addShaped(
            px("yellow_power_flower"),
            "CLC",
            "RRR",
            "RRR",
            'C',
            px("yellow_compressed_collector"),
            'L',
            px("yellow_emc_link"),
            'R',
            px("yellow_relay"));
        addShaped(
            px("yellow_power_flower"),
            "CLC",
            "RPR",
            "RRR",
            'C',
            px("yellow_compressed_collector"),
            'L',
            px("yellow_emc_link"),
            'P',
            px("lime_power_flower"),
            'R',
            px("yellow_relay"));
        addShaped(
            px("basic_relay"),
            "OSO",
            "OAO",
            "OOO",
            'A',
            pe("aeternalis_fuel_block"),
            'O',
            "obsidian",
            'S',
            "blockGlass");
        addShapeless(px("blue_relay"), px("violet_relay"), px("blue_matter"));
        addShapeless(px("cyan_relay"), px("blue_relay"), px("cyan_matter"));
        addShapeless(px("dark_relay"), px("basic_relay"), pe("dark_matter"));
        addShapeless(px("fading_relay"), px("white_relay"), px("fading_matter"));
        addShapeless(px("final_relay"), px("fading_relay"), px("final_star_shard"));
        addShapeless(px("green_relay"), px("cyan_relay"), px("green_matter"));
        addShapeless(px("lime_relay"), px("green_relay"), px("lime_matter"));
        addShapeless(px("magenta_relay"), px("red_relay"), px("magenta_matter"));
        addShapeless(px("basic_relay"), pe("relay_mk1"));
        addShapeless(px("dark_relay"), pe("relay_mk2"));
        addShapeless(px("red_relay"), pe("relay_mk3"));
        addShapeless(px("orange_relay"), px("yellow_relay"), px("orange_matter"));
        addShapeless(px("pink_relay"), px("magenta_relay"), px("pink_matter"));
        addShapeless(px("purple_relay"), px("pink_relay"), px("purple_matter"));
        addShapeless(px("red_relay"), px("dark_relay"), pe("red_matter"));
        addShapeless(px("violet_relay"), px("purple_relay"), px("violet_matter"));
        addShapeless(px("white_relay"), px("orange_relay"), px("white_matter"));
        addShapeless(px("yellow_relay"), px("lime_relay"), px("yellow_matter"));
        addShapeless(
            px("colossal_star_drei"),
            px("colossal_star_zwei"),
            px("colossal_star_zwei"),
            px("colossal_star_zwei"),
            px("colossal_star_zwei"));
        addShapeless(
            px("colossal_star_ein"),
            px("magnum_star_omega"),
            px("magnum_star_omega"),
            px("magnum_star_omega"),
            px("magnum_star_omega"));
        addShapeless(
            px("colossal_star_omega"),
            px("colossal_star_sphere"),
            px("colossal_star_sphere"),
            px("colossal_star_sphere"),
            px("colossal_star_sphere"));
        addShapeless(
            px("colossal_star_sphere"),
            px("colossal_star_vier"),
            px("colossal_star_vier"),
            px("colossal_star_vier"),
            px("colossal_star_vier"));
        addShapeless(
            px("colossal_star_vier"),
            px("colossal_star_drei"),
            px("colossal_star_drei"),
            px("colossal_star_drei"),
            px("colossal_star_drei"));
        addShapeless(
            px("colossal_star_zwei"),
            px("colossal_star_ein"),
            px("colossal_star_ein"),
            px("colossal_star_ein"),
            px("colossal_star_ein"));
        addShaped(px("final_star"), "SSS", "SES", "SSS", 'E', mc("dragon_egg"), 'S', px("final_power_flower"));
        addShaped(px("final_star_shard"), "SSS", "SNS", "SSS", 'N', mc("nether_star"), 'S', px("colossal_star_omega"));
        addShapeless(
            px("gargantuan_star_drei"),
            px("gargantuan_star_zwei"),
            px("gargantuan_star_zwei"),
            px("gargantuan_star_zwei"),
            px("gargantuan_star_zwei"));
        addShapeless(
            px("gargantuan_star_ein"),
            px("colossal_star_omega"),
            px("colossal_star_omega"),
            px("colossal_star_omega"),
            px("colossal_star_omega"));
        addShapeless(
            px("gargantuan_star_omega"),
            px("gargantuan_star_sphere"),
            px("gargantuan_star_sphere"),
            px("gargantuan_star_sphere"),
            px("gargantuan_star_sphere"));
        addShapeless(
            px("gargantuan_star_sphere"),
            px("gargantuan_star_vier"),
            px("gargantuan_star_vier"),
            px("gargantuan_star_vier"),
            px("gargantuan_star_vier"));
        addShapeless(
            px("gargantuan_star_vier"),
            px("gargantuan_star_drei"),
            px("gargantuan_star_drei"),
            px("gargantuan_star_drei"),
            px("gargantuan_star_drei"));
        addShapeless(
            px("gargantuan_star_zwei"),
            px("gargantuan_star_ein"),
            px("gargantuan_star_ein"),
            px("gargantuan_star_ein"),
            px("gargantuan_star_ein"));
        addShapeless(
            px("magnum_star_drei"),
            px("magnum_star_zwei"),
            px("magnum_star_zwei"),
            px("magnum_star_zwei"),
            px("magnum_star_zwei"));
        addShapeless(
            px("magnum_star_ein"),
            pe("klein_star_omega"),
            pe("klein_star_omega"),
            pe("klein_star_omega"),
            pe("klein_star_omega"));
        addShapeless(
            px("magnum_star_omega"),
            px("magnum_star_sphere"),
            px("magnum_star_sphere"),
            px("magnum_star_sphere"),
            px("magnum_star_sphere"));
        addShapeless(
            px("magnum_star_sphere"),
            px("magnum_star_vier"),
            px("magnum_star_vier"),
            px("magnum_star_vier"),
            px("magnum_star_vier"));
        addShapeless(
            px("magnum_star_vier"),
            px("magnum_star_drei"),
            px("magnum_star_drei"),
            px("magnum_star_drei"),
            px("magnum_star_drei"));
        addShapeless(
            px("magnum_star_zwei"),
            px("magnum_star_ein"),
            px("magnum_star_ein"),
            px("magnum_star_ein"),
            px("magnum_star_ein"));
        addShaped(
            px("transmutation_interface"),
            "LCL",
            "CSC",
            "LCL",
            'C',
            pe("condenser_mk2"),
            'L',
            px("final_emc_link"),
            'S',
            px("final_star_shard"));
    }

    // 材料解析

    private static ItemStack st(ItemStack base, int count) {
        ItemStack out = base.copy();
        out.stackSize = count;
        return out;
    }

    private static ItemStack px(String name) {
        return new ItemStack(pxItem(name));
    }

    private static Item pxItem(String name) {
        Item i = pxSingle(name);
        if (i != null) return i;
        if (name.endsWith("_compressed_collector")) {
            return PXItems.COMPRESSED_COLLECTORS.get(matter(tier(name, "_compressed_collector")));
        }
        if (name.endsWith("_power_flower")) {
            return Item.getItemFromBlock(PXItems.POWER_FLOWERS.get(matter(tier(name, "_power_flower"))));
        }
        if (name.endsWith("_emc_link")) {
            return Item.getItemFromBlock(PXItems.EMC_LINKS.get(matter(tier(name, "_emc_link"))));
        }
        if (name.endsWith("_advanced_alchemical_chest")) {
            return Item.getItemFromBlock(PXItems.CHESTS.get(tier(name, "_advanced_alchemical_chest")));
        }
        if (name.endsWith("_matter_block")) {
            return Item.getItemFromBlock(PXItems.MATTER_BLOCKS.get(matter(tier(name, "_matter_block"))));
        }
        if (name.endsWith("_matter")) {
            return PXItems.MATTER_ITEMS.get(matter(tier(name, "_matter")));
        }
        if (name.endsWith("_fuel_block")) {
            return Item.getItemFromBlock(PXItems.FUEL_BLOCKS.get(fuel(tier(name, "_fuel_block"))));
        }
        if (name.endsWith("_fuel")) {
            return PXItems.FUEL_ITEMS.get(fuel(tier(name, "_fuel")));
        }
        if (name.endsWith("_collector")) {
            return Item.getItemFromBlock(PXItems.COLLECTORS.get(matter(tier(name, "_collector"))));
        }
        if (name.endsWith("_relay")) {
            return Item.getItemFromBlock(PXItems.RELAYS.get(matter(tier(name, "_relay"))));
        }
        if (name.matches("(magnum|colossal|gargantuan)_star_(ein|zwei|drei|vier|sphere|omega)")) {
            String[] parts = name.split("_");
            Star.StarType type = Star.StarType.valueOf(parts[0].toUpperCase(Locale.US));
            Star star = Star.valueOf(parts[2].toUpperCase(Locale.US));
            return PXItems.STARS[type.ordinal()][star.ordinal()];
        }
        throw new IllegalArgumentException("unmapped expansion item: " + name);
    }

    private static String tier(String name, String suffix) {
        return name.substring(0, name.length() - suffix.length());
    }

    private static Matter matter(String name) {
        return Matter.valueOf(name.toUpperCase(Locale.US));
    }

    private static Fuel fuel(String name) {
        return Fuel.valueOf(name.toUpperCase(Locale.US));
    }

    private static Item pxSingle(String name) {
        switch (name) {
            case "final_star":
                return PXItems.finalStar;
            case "final_star_shard":
                return PXItems.finalStarShard;
            case "knowledge_sharing_book":
                return PXItems.knowledgeSharingBook;
            case "arcane_transmutation_tablet":
                return PXItems.arcaneTransmutationTablet;
            case "basic_alchemical_book":
                return PXItems.basicAlchemicalBook;
            case "advanced_alchemical_book":
                return PXItems.advancedAlchemicalBook;
            case "master_alchemical_book":
                return PXItems.masterAlchemicalBook;
            case "arcane_alchemical_book":
                return PXItems.arcaneAlchemicalBook;
            case "transmutation_interface":
                return Item.getItemFromBlock(PXItems.transmutationInterface);
            case "compact_sun":
                return Item.getItemFromBlock(PXItems.compactSun);
            case "condenser_mk3":
                return Item.getItemFromBlock(PXItems.condenserMK3);
            default:
                return null;
        }
    }

    /** ProjectE 1.7.10 (PE1.10.1) 物品引用。 */
    private static ItemStack pe(String name) {
        switch (name) {
            case "low_covalence_dust":
                return new ItemStack(ObjHandler.covalence, 1, 0);
            case "medium_covalence_dust":
                return new ItemStack(ObjHandler.covalence, 1, 1);
            case "high_covalence_dust":
                return new ItemStack(ObjHandler.covalence, 1, 2);
            case "dark_matter":
                return new ItemStack(ObjHandler.matter, 1, 0);
            case "red_matter":
                return new ItemStack(ObjHandler.matter, 1, 1);
            case "alchemical_coal":
                return new ItemStack(ObjHandler.fuels, 1, 0);
            case "mobius_fuel":
                return new ItemStack(ObjHandler.fuels, 1, 1);
            case "aeternalis_fuel":
                return new ItemStack(ObjHandler.fuels, 1, 2);
            case "aeternalis_fuel_block":
                return new ItemStack(Item.getItemFromBlock(ObjHandler.fuelBlock), 1, 2);
            case "philosophers_stone":
                return new ItemStack(ObjHandler.philosStone);
            case "transmutation_tablet":
                return new ItemStack(ObjHandler.transmutationTablet);
            case "klein_star_omega":
                return new ItemStack(ObjHandler.kleinStars, 1, 5);
            case "collector_mk1":
                return new ItemStack(Item.getItemFromBlock(ObjHandler.energyCollector));
            case "collector_mk2":
                return new ItemStack(Item.getItemFromBlock(ObjHandler.collectorMK2));
            case "collector_mk3":
                return new ItemStack(Item.getItemFromBlock(ObjHandler.collectorMK3));
            case "relay_mk1":
                return new ItemStack(Item.getItemFromBlock(ObjHandler.relay));
            case "relay_mk2":
                return new ItemStack(Item.getItemFromBlock(ObjHandler.relayMK2));
            case "relay_mk3":
                return new ItemStack(Item.getItemFromBlock(ObjHandler.relayMK3));
            case "condenser_mk1":
                return new ItemStack(Item.getItemFromBlock(ObjHandler.condenser));
            case "condenser_mk2":
                return new ItemStack(Item.getItemFromBlock(ObjHandler.condenserMk2));
        }
        if (name.endsWith("_alchemical_bag")) {
            String color = name.substring(0, name.length() - "_alchemical_bag".length());
            int dmg;
            switch (color) {
                case "white":
                    dmg = 0;
                    break;
                case "orange":
                    dmg = 1;
                    break;
                case "magenta":
                    dmg = 2;
                    break;
                case "light_blue":
                    dmg = 3;
                    break;
                case "yellow":
                    dmg = 4;
                    break;
                case "lime":
                    dmg = 5;
                    break;
                case "pink":
                    dmg = 6;
                    break;
                case "gray":
                    dmg = 7;
                    break;
                case "light_gray":
                    dmg = 8;
                    break;
                case "cyan":
                    dmg = 9;
                    break;
                case "purple":
                    dmg = 10;
                    break;
                case "blue":
                    dmg = 11;
                    break;
                case "brown":
                    dmg = 12;
                    break;
                case "green":
                    dmg = 13;
                    break;
                case "red":
                    dmg = 14;
                    break;
                case "black":
                    dmg = 15;
                    break;
                default:
                    throw new IllegalArgumentException("unmapped bag color: " + color);
            }
            return new ItemStack(ObjHandler.alchBag, 1, dmg);
        }
        throw new IllegalArgumentException("unmapped ProjectE item: " + name);
    }

    /** 原版 1.7.10 引用(染料是单个 'dye' 物品 + 损伤值)。 */
    private static ItemStack mc(String name) {
        switch (name) {
            case "book":
                return new ItemStack(Items.book);
            case "coal":
                return new ItemStack(Items.coal);
            case "cooked_beef":
                return new ItemStack(Items.cooked_beef);
            case "ender_pearl":
                return new ItemStack(Items.ender_pearl);
            case "nether_star":
                return new ItemStack(Items.nether_star);
            case "furnace":
                return new ItemStack(Item.getItemFromBlock(Blocks.furnace));
            case "glowstone":
                return new ItemStack(Item.getItemFromBlock(Blocks.glowstone));
            case "dragon_egg":
                return new ItemStack(Item.getItemFromBlock(Blocks.dragon_egg));
        }
        int dmg;
        switch (name) {
            case "black_dye":
                dmg = 0;
                break;
            case "red_dye":
                dmg = 1;
                break;
            case "green_dye":
                dmg = 2;
                break;
            case "brown_dye":
                dmg = 3;
                break;
            case "blue_dye":
                dmg = 4;
                break;
            case "purple_dye":
                dmg = 5;
                break;
            case "cyan_dye":
                dmg = 6;
                break;
            case "light_gray_dye":
                dmg = 7;
                break;
            case "gray_dye":
                dmg = 8;
                break;
            case "pink_dye":
                dmg = 9;
                break;
            case "lime_dye":
                dmg = 10;
                break;
            case "yellow_dye":
                dmg = 11;
                break;
            case "light_blue_dye":
                dmg = 12;
                break;
            case "magenta_dye":
                dmg = 13;
                break;
            case "orange_dye":
                dmg = 14;
                break;
            case "white_dye":
                dmg = 15;
                break;
            default:
                throw new IllegalArgumentException("unmapped vanilla item: " + name);
        }
        return new ItemStack(Items.dye, 1, dmg);
    }

    // 价值配方记录(供 PXEmc 定价使用)

    private static final java.util.List<ValueRecipe> VALUE_RECIPES = new java.util.ArrayList<>();

    /** 输出 + 行主序的槽位;每个槽位 = 候选(ItemStack 或矿物词典 String)。 */
    static final class ValueRecipe {

        final ItemStack out;
        final java.util.List<java.util.List<Object>> slots;

        ValueRecipe(ItemStack out, java.util.List<java.util.List<Object>> slots) {
            this.out = out;
            this.slots = slots;
        }
    }

    static java.util.List<ValueRecipe> valueRecipes() {
        return VALUE_RECIPES;
    }

    private static void noteShaped(ItemStack out, Object[] recipe) {
        int i = 0;
        java.util.List<String> rows = new java.util.ArrayList<>();
        while (i < recipe.length && recipe[i] instanceof String) {
            rows.add((String) recipe[i]);
            i++;
        }
        java.util.Map<Character, Object> key = new java.util.HashMap<>();
        for (; i + 1 < recipe.length; i += 2) {
            key.put((Character) recipe[i], recipe[i + 1]);
        }
        java.util.List<java.util.List<Object>> slots = new java.util.ArrayList<>();
        for (String row : rows) {
            for (int c = 0; c < row.length(); c++) {
                java.util.List<Object> opts = new java.util.ArrayList<>();
                char ch = row.charAt(c);
                if (ch != ' ') {
                    Object v = key.get(ch);
                    if (v != null) opts.add(v);
                }
                slots.add(opts);
            }
        }
        VALUE_RECIPES.add(new ValueRecipe(out, slots));
    }

    private static void noteShapeless(ItemStack out, Object[] recipe) {
        java.util.List<java.util.List<Object>> slots = new java.util.ArrayList<>();
        for (Object o : recipe) {
            java.util.List<Object> opts = new java.util.ArrayList<>();
            opts.add(o);
            slots.add(opts);
        }
        VALUE_RECIPES.add(new ValueRecipe(out, slots));
    }

    // 添加器

    private static void addShaped(ItemStack out, Object... recipe) {
        GameRegistry.addRecipe(new ShapedOreRecipe(out, recipe));
        noteShaped(out, recipe);
    }

    private static void addShapeless(ItemStack out, Object... recipe) {
        GameRegistry.addRecipe(new ShapelessOreRecipe(out, recipe));
        noteShapeless(out, recipe);
    }
}
