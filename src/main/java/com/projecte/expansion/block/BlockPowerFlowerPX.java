package com.projecte.expansion.block;

import java.util.Locale;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import com.projecte.expansion.MyMod;
import com.projecte.expansion.tile.TileEntityPowerFlowerPX;
import com.projecte.expansion.util.Matter;

/**
 * 力量花:上游将其渲染为组合模型(继电器贴图的底座环,外加收集器贴图的花朵),
 * 因此它是 BlockContainer,由 PowerFlowerRendererPX 绘制,而不是普通立方体。
 * 方块图标仅用于粒子效果 / 兜底回退。
 */
public class BlockPowerFlowerPX extends BlockContainer {

    public final Matter matter;
    private final String tier;

    public BlockPowerFlowerPX(Matter matter) {
        super(Material.iron);
        this.matter = matter;
        this.tier = matter.name()
            .toLowerCase(Locale.US);
        setBlockName(tier + "_power_flower");
        setHardness(5.0F);
        setResistance(10.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityPowerFlowerPX();
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int getRenderType() {
        return -1;
    }

    @Override
    public void registerBlockIcons(IIconRegister register) {
        blockIcon = register.registerIcon(MyMod.MODID + ":relay/" + tier);
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return blockIcon;
    }
}
