package com.projecte.expansion;

import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;

import com.projecte.expansion.client.ChestLikeItemRendererPX;
import com.projecte.expansion.client.ChestLikeRendererPX;
import com.projecte.expansion.client.CompressedCollectorItemRendererPX;
import com.projecte.expansion.client.PowerFlowerItemRendererPX;
import com.projecte.expansion.client.PowerFlowerRendererPX;
import com.projecte.expansion.registry.PXItems;
import com.projecte.expansion.tile.TileEntityAdvancedChestPX;
import com.projecte.expansion.tile.TileEntityCondenserMK3PX;
import com.projecte.expansion.tile.TileEntityPowerFlowerPX;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        // 箱子形状的方块:高级炼金箱 + 能量凝聚器 MK3
        ChestLikeRendererPX chestRenderer = new ChestLikeRendererPX();
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAdvancedChestPX.class, chestRenderer);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCondenserMK3PX.class, chestRenderer);
        ChestLikeItemRendererPX chestItemRenderer = new ChestLikeItemRendererPX();
        for (Item chestItem : PXItems.CHEST_ITEMS.keySet()) {
            MinecraftForgeClient.registerItemRenderer(chestItem, chestItemRenderer);
        }
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(PXItems.condenserMK3), chestItemRenderer);
        // 压缩收集器按其收集器方块渲染
        CompressedCollectorItemRendererPX compressedRenderer = new CompressedCollectorItemRendererPX();
        for (Item compressedCollector : PXItems.COMPRESSED_COLLECTOR_BLOCKS.keySet()) {
            MinecraftForgeClient.registerItemRenderer(compressedCollector, compressedRenderer);
        }
        // 力量花使用上游的组合模型
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPowerFlowerPX.class, new PowerFlowerRendererPX());
        PowerFlowerItemRendererPX flowerItemRenderer = new PowerFlowerItemRendererPX();
        for (Item flower : PXItems.POWER_FLOWER_ITEMS.keySet()) {
            MinecraftForgeClient.registerItemRenderer(flower, flowerItemRenderer);
        }
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

    @Override
    public void serverStarting(FMLServerStartingEvent event) {
        super.serverStarting(event);
    }
}
