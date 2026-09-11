package com.projecte.expansion;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

/**
 * 对应上游 Project Expansion 服务器/客户端选项的 Forge 配置(config)。
 * 在 preInit 中加载;分类为 "server" 和 "client"。
 */
public final class Config {

    private Config() {}

    // 服务端
    public static int tickDelay = 20;
    public static boolean notifyCommandChanges = true;
    public static boolean notifyKnowledgeBookGains = true;
    public static boolean limitEmcLinkVendor = true;
    public static boolean enableFluidEfficiency = true;
    public static int transmutationInterfaceItemCount = Integer.MAX_VALUE;
    public static double collectorMultiplier = 1.0D;
    public static double emcLinkItemLimitMultiplier = 1.0D;
    public static double emcLinkFluidLimitMultiplier = 1.0D;
    public static double emcLinkEMCLimitMultiplier = 1.0D;
    public static double powerflowerMultiplier = 1.0D;
    public static double relayBonusMultiplier = 1.0D;
    public static double relayTransferMultiplier = 1.0D;
    public static boolean persistEnchantedBooksOnly = false;
    public static String editOthersAlchemicalBooks = "DISABLED";
    public static boolean zeroEmcFluidsAreFree = true;
    public static boolean enableCollectorOptimizations = false;
    public static int compactSunBonus = 10;
    public static boolean sunMultiplierPriceCompensation = true;
    public static boolean enableReloadEMCCommand = true;

    // 客户端
    public static boolean formatEMC = true;
    public static boolean fullNumberNames = true;
    public static boolean emcDisplay = true;
    public static String emcDisplayPosition = "TOP_LEFT";
    public static boolean enableLearnedTooltip = true;
    public static boolean alchemicalCollectionSound = true;
    public static String searchType = "NORMAL";

    public static void load(File suggestedFile) {
        Configuration cfg = new Configuration(suggestedFile);
        try {
            cfg.load();

            final String server = "server";
            tickDelay = cfg.getInt(
                "tickDelay",
                server,
                tickDelay,
                1,
                200,
                "The delay between mod operations (in ticks, default 20) - this will slightly affect the amount of emc generated via rounding - increase if you're noticing lag");
            notifyCommandChanges = cfg.getBoolean(
                "notifyCommandChanges",
                server,
                notifyCommandChanges,
                "Notify users when something is changed about them via commands.");
            notifyKnowledgeBookGains = cfg.getBoolean(
                "notifyKnowledgeBookGains",
                server,
                notifyKnowledgeBookGains,
                "Tell users the list of items they gained when using a knowledge book.");
            limitEmcLinkVendor = cfg.getBoolean(
                "limitEmcLinkVendor",
                server,
                limitEmcLinkVendor,
                "If EMC Link Right-Click functionality should be Limited by Tier or Not.");
            enableFluidEfficiency = cfg.getBoolean(
                "enableFluidEfficiency",
                server,
                enableFluidEfficiency,
                "If fluid efficiency should be enabled.");
            transmutationInterfaceItemCount = cfg.getInt(
                "transmutationInterfaceItemCount",
                server,
                transmutationInterfaceItemCount,
                1,
                Integer.MAX_VALUE,
                "The amount of items that the transmutation interface will report to have.");
            collectorMultiplier = cfg
                .get(
                    "collectorMultiplier",
                    server,
                    collectorMultiplier,
                    "Multiplies the output of Collectors.",
                    0.1D,
                    50.0D)
                .getDouble();
            emcLinkItemLimitMultiplier = cfg
                .get(
                    "emcLinkItemLimitMultiplier",
                    server,
                    emcLinkItemLimitMultiplier,
                    "Multiplies the item limit of EMC Links.",
                    0.1D,
                    50.0D)
                .getDouble();
            emcLinkFluidLimitMultiplier = cfg
                .get(
                    "emcLinkFluidLimitMultiplier",
                    server,
                    emcLinkFluidLimitMultiplier,
                    "Multiplies the fluid limit of EMC Links.",
                    0.1D,
                    50.0D)
                .getDouble();
            emcLinkEMCLimitMultiplier = cfg
                .get(
                    "emcLinkEMCLimitMultiplier",
                    server,
                    emcLinkEMCLimitMultiplier,
                    "Multiplies the emc limit of EMC Links.",
                    0.1D,
                    50.0D)
                .getDouble();
            powerflowerMultiplier = cfg
                .get(
                    "powerflowerMultiplier",
                    server,
                    powerflowerMultiplier,
                    "Multiplies the output of Power Flowers.",
                    0.1D,
                    50.0D)
                .getDouble();
            relayBonusMultiplier = cfg
                .get(
                    "relayBonusMultiplier",
                    server,
                    relayBonusMultiplier,
                    "Multiplies the bonus of Relays.",
                    0.1D,
                    50.0D)
                .getDouble();
            relayTransferMultiplier = cfg
                .get(
                    "relayTransferMultiplier",
                    server,
                    relayTransferMultiplier,
                    "Multiplies the transfer limit of Relays.",
                    0.1D,
                    50.0D)
                .getDouble();
            persistEnchantedBooksOnly = cfg.getBoolean(
                "persistEnchantedBooksOnly",
                server,
                persistEnchantedBooksOnly,
                "If ProjectE's processors.EnchantmentProcessor.persistent option should only include enchanted books.");
            editOthersAlchemicalBooks = cfg.getString(
                "editOthersAlchemicalBooks",
                server,
                editOthersAlchemicalBooks,
                "If players should be allowed to edit books bound to other players. Valid: DISABLED, OP_ONLY, ENABLED");
            zeroEmcFluidsAreFree = cfg.getBoolean(
                "zeroEmcFluidsAreFree",
                server,
                zeroEmcFluidsAreFree,
                "If fluids which end their calculations at zero emc should be returned as free.");
            enableCollectorOptimizations = cfg.getBoolean(
                "enableCollectorOptimizations",
                server,
                enableCollectorOptimizations,
                "If optimizations (ticking only once per second) should be enabled for collectors.");
            compactSunBonus = cfg.getInt(
                "compactSunBonus",
                server,
                compactSunBonus,
                0,
                Integer.MAX_VALUE,
                "The bonus (multiplicative) the compact sun block should give. Set to 0 to disable.");
            sunMultiplierPriceCompensation = cfg.getBoolean(
                "sunMultiplierPriceCompensation",
                server,
                sunMultiplierPriceCompensation,
                "Enable determining the sun bonus multiplier via the difference in emc price between the final power flower and the compact sun block.");
            enableReloadEMCCommand = cfg.getBoolean(
                "enableReloadEMCCommand",
                server,
                enableReloadEMCCommand,
                "Enable the /projectexpansion reloademc command. This is experimental.");

            final String client = "client";
            formatEMC = cfg.getBoolean("formatEMC", client, formatEMC, "If EMC should be formatted as M/B/T/etc");
            fullNumberNames = cfg.getBoolean(
                "fullNumberNames",
                client,
                fullNumberNames,
                "If full number names (Million/Billion/Trillion) should be used instead of abbreviations");
            emcDisplay = cfg.getBoolean(
                "emcDisplay",
                client,
                emcDisplay,
                "Displays your current emc and gained emc per second in the top left corner.");
            emcDisplayPosition = cfg.getString(
                "emcDisplayPosition",
                client,
                emcDisplayPosition,
                "The Position of the emc display. Valid: TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT");
            enableLearnedTooltip = cfg.getBoolean(
                "enableLearnedTooltip",
                client,
                enableLearnedTooltip,
                "If a tooltip should be shown on items which can be learned, denoting if the item has been learned or not.");
            alchemicalCollectionSound = cfg.getBoolean(
                "alchemicalCollectionSound",
                client,
                alchemicalCollectionSound,
                "If a sound should be played when something is collected with Alchemical Collection.");
            searchType = cfg.getString(
                "searchType",
                client,
                searchType,
                "Search type. Valid: NORMAL, NORMAL_AUTOFOCUS, SYNC, SYNC_AUTOFOCUS");
        } finally {
            if (cfg.hasChanged()) {
                cfg.save();
            }
        }
    }
}
