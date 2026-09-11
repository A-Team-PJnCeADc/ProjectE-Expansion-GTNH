package com.projecte.expansion.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import com.projecte.expansion.MyMod;
import com.projecte.expansion.tile.TileEntityAdvancedChestPX;

/**
 * 高级炼金箱,每种染料颜色一个方块。渲染为 3D 箱子
 * (ChestRendererPX);朝向存于 metadata,遵循原版箱子的约定
 * (2=北, 3=南, 4=西, 5=东)。
 */
public class BlockAdvancedAlchemicalChestPX extends BlockContainer {

    public final String color;

    public BlockAdvancedAlchemicalChestPX(String color) {
        super(Material.wood);
        this.color = color;
        setBlockName(color + "_advanced_alchemical_chest");
        setHardness(2.5F);
        setResistance(5.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityAdvancedChestPX();
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
        blockIcon = register.registerIcon(MyMod.MODID + ":advanced_alchemical_chest/" + color);
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return blockIcon;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        int dir = MathHelper.floor_double((double) (placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        int meta;
        switch (dir) {
            case 0:
                meta = 2; // 北
                break;
            case 1:
                meta = 5; // 东
                break;
            case 2:
                meta = 3; // 南
                break;
            default:
                meta = 4; // 西
                break;
        }
        world.setBlockMetadataWithNotify(x, y, z, meta, 2);
    }
}
