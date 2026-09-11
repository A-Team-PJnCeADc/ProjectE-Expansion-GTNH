package com.projecte.expansion.client;

import java.util.Locale;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import com.projecte.expansion.client.model.PowerFlowerModelPX;
import com.projecte.expansion.registry.PXItems;
import com.projecte.expansion.util.Matter;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 力量花的 3D 物品渲染(物品栏、创造模式物品栏、手持、掉落物),使用与世界中方块相同的
 * 组合模型。
 */
@SideOnly(Side.CLIENT)
public class PowerFlowerItemRendererPX implements IItemRenderer {

    private static final String DOMAIN = "projectexpansiongtnh";
    private final PowerFlowerModelPX model = new PowerFlowerModelPX();

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return helper == ItemRendererHelper.INVENTORY_BLOCK || helper == ItemRendererHelper.EQUIPPED_BLOCK
            || helper == ItemRendererHelper.ENTITY_ROTATION;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        Matter matter = PXItems.POWER_FLOWER_ITEMS.get(item.getItem());
        if (matter == null) {
            return;
        }
        String tier = matter.name()
            .toLowerCase(Locale.US);
        GL11.glPushMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        switch (type) {
            case ENTITY:
            case INVENTORY:
                // 位置由 Forge 的方块辅助方法设定
                break;
            default:
                GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
                GL11.glScalef(0.75F, 0.75F, 0.75F);
                break;
        }
        FMLClientHandler.instance()
            .getClient().renderEngine
                .bindTexture(new ResourceLocation(DOMAIN, "textures/blocks/relay/" + tier + ".png"));
        model.renderTexture(0);
        FMLClientHandler.instance()
            .getClient().renderEngine
                .bindTexture(new ResourceLocation(DOMAIN, "textures/blocks/collector/" + tier + ".png"));
        model.renderTexture(1);
        GL11.glPopMatrix();
    }
}
