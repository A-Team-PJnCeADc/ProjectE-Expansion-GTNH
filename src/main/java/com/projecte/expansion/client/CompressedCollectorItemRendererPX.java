package com.projecte.expansion.client;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import com.projecte.expansion.registry.PXItems;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 压缩收集器渲染为完整的收集器方块立方体(上游物品模型
 * parent = block/collector/&lt;tier&gt;),而不是平面图标。
 */
@SideOnly(Side.CLIENT)
public class CompressedCollectorItemRendererPX implements IItemRenderer {

    private final RenderBlocks blockRenderer = new RenderBlocks();

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
        Block block = PXItems.COMPRESSED_COLLECTOR_BLOCKS.get(item.getItem());
        if (block == null) {
            return;
        }
        GL11.glPushMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        // RenderBlocks 采样的是方块 atlas,不是物品 atlas
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        switch (type) {
            case INVENTORY:
            case ENTITY:
                // 由上面的 Forge 方块辅助标志完成定位
                break;
            default:
                // 装备/第一人称:把 0..1 的立方体居中到手部空间
                GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
                GL11.glScalef(0.8F, 0.8F, 0.8F);
                break;
        }
        blockRenderer.renderBlockAsItem(block, 0, 1.0F);
        GL11.glPopMatrix();
    }
}
