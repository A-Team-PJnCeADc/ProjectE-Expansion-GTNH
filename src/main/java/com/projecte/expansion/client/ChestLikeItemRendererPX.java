package com.projecte.expansion.client;

import net.minecraft.client.model.ModelChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import com.projecte.expansion.registry.PXItems;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 箱子的 3D 物品渲染(物品栏、创造模式物品栏、手持、掉落物),使用原版 ModelChest,
 * 与 ProjectE 的 ChestItemRenderer 保持一致——箱子的贴图集已转换为该模型的版式。
 */
@SideOnly(Side.CLIENT)
public class ChestLikeItemRendererPX implements IItemRenderer {

    private static final String DOMAIN = "projectexpansiongtnh";
    private final ModelChest model = new ModelChest();

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        ResourceLocation texture = textureFor(item.getItem());
        if (texture == null) {
            return;
        }
        switch (type) {
            case ENTITY:
                renderChest(texture, 0.5F, 0.5F, 0.5F);
                break;
            case EQUIPPED:
            case EQUIPPED_FIRST_PERSON:
                renderChest(texture, 1.0F, 1.0F, 1.0F);
                break;
            case INVENTORY:
                renderChest(texture, 0.0F, 0.075F, 0.0F);
                break;
            default:
                break;
        }
    }

    private void renderChest(ResourceLocation texture, float x, float y, float z) {
        FMLClientHandler.instance()
            .getClient().renderEngine.bindTexture(texture);
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, z);
        GL11.glRotatef(180, 1, 0, 0);
        GL11.glRotatef(-90, 0, 1, 0);
        model.renderAll();
        GL11.glPopMatrix();
    }

    private ResourceLocation textureFor(Item item) {
        String color = PXItems.CHEST_ITEMS.get(item);
        if (color != null) {
            return new ResourceLocation(DOMAIN, "textures/blocks/advanced_alchemical_chest/" + color + ".png");
        }
        if (PXItems.condenserMK3 != null && item == Item.getItemFromBlock(PXItems.condenserMK3)) {
            return new ResourceLocation(DOMAIN, "textures/blocks/condenser_mk3.png");
        }
        return null;
    }
}
