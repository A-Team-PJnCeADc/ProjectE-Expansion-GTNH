package com.projecte.expansion.client;

import net.minecraft.block.Block;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.projecte.expansion.block.BlockAdvancedAlchemicalChestPX;
import com.projecte.expansion.tile.TileEntityAdvancedChestPX;
import com.projecte.expansion.tile.TileEntityCondenserMK3PX;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 使用原版 ModelChest 渲染箱子形状的方块（高级炼金箱、能量凝聚器 MK3），
 * 与 ProjectE 在 1.7.10 中的 ChestRenderer/CondenserRenderer 完全一致：
 * 贴图集会被转换为该模型所采样的布局（tools/convert_chest_textures.py），
 * 因此几何形状与原版箱子保持完全一致。
 */
@SideOnly(Side.CLIENT)
public class ChestLikeRendererPX extends TileEntitySpecialRenderer {

    private static final String DOMAIN = "projectexpansiongtnh";
    private final ModelChest model = new ModelChest();

    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTick) {
        ResourceLocation texture = textureFor(tile);
        if (texture == null) {
            return;
        }
        bindTexture(texture);

        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glTranslatef((float) x, (float) y + 1.0F, (float) z + 1.0F);
        GL11.glScalef(1.0F, -1.0F, -1.0F);
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        GL11.glRotatef(facingAngle(tile.getBlockMetadata()), 0.0F, 1.0F, 0.0F);
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        model.chestLid.rotateAngleX = 0.0F;
        model.renderAll();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /** 与原版箱子渲染器 / ProjectE 冷凝器渲染器相同的角度表。 */
    private static float facingAngle(int meta) {
        switch (meta) {
            case 2: // 北
                return 180.0F;
            case 3: // 南
                return 0.0F;
            case 4: // 西
                return 90.0F;
            case 5: // 东
                return -90.0F;
            default:
                return 0.0F;
        }
    }

    private ResourceLocation textureFor(TileEntity tile) {
        if (tile instanceof TileEntityCondenserMK3PX) {
            return new ResourceLocation(DOMAIN, "textures/blocks/condenser_mk3.png");
        }
        if (tile instanceof TileEntityAdvancedChestPX && tile.getWorldObj() != null) {
            Block block = tile.getWorldObj()
                .getBlock(tile.xCoord, tile.yCoord, tile.zCoord);
            if (block instanceof BlockAdvancedAlchemicalChestPX) {
                return new ResourceLocation(
                    DOMAIN,
                    "textures/blocks/advanced_alchemical_chest/" + ((BlockAdvancedAlchemicalChestPX) block).color
                        + ".png");
            }
        }
        return null;
    }
}
