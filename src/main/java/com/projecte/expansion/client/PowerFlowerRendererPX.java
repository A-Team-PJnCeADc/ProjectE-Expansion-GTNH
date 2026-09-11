package com.projecte.expansion.client;

import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.projecte.expansion.block.BlockPowerFlowerPX;
import com.projecte.expansion.client.model.PowerFlowerModelPX;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 使用上游的复合模型渲染力量花：继电器贴图的底座圆环，
 * 其上方是收集器贴图的花朵。
 */
@SideOnly(Side.CLIENT)
public class PowerFlowerRendererPX extends TileEntitySpecialRenderer {

    private static final String DOMAIN = "projectexpansiongtnh";
    private final PowerFlowerModelPX model = new PowerFlowerModelPX();

    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTick) {
        if (tile.getWorldObj() == null) {
            return;
        }
        Block block = tile.getWorldObj()
            .getBlock(tile.xCoord, tile.yCoord, tile.zCoord);
        if (!(block instanceof BlockPowerFlowerPX)) {
            return;
        }
        String tier = ((BlockPowerFlowerPX) block).matter.name()
            .toLowerCase(Locale.US);
        renderFlower(x, y, z, tier);
    }

    private void renderFlower(double x, double y, double z, String tier) {
        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glTranslated(x, y, z);
        bind(new ResourceLocation(DOMAIN, "textures/blocks/relay/" + tier + ".png"));
        model.renderTexture(0);
        bind(new ResourceLocation(DOMAIN, "textures/blocks/collector/" + tier + ".png"));
        model.renderTexture(1);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void bind(ResourceLocation texture) {
        cpw.mods.fml.client.FMLClientHandler.instance()
            .getClient().renderEngine.bindTexture(texture);
    }
}
