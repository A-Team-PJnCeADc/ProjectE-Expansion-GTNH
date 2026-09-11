package com.projecte.expansion.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

import com.projecte.expansion.MyMod;

/**
 * 基础方块,各面使用相同的简单图标,1.7.10 风格。
 * 图标路径相对于 assets/<modid>/textures/blocks/。
 */
public class BlockPXBase extends Block {

    protected final String[] iconPaths;
    protected IIcon[] icons;

    public BlockPXBase(String name, Material material, float hardness, float resistance, String... iconPaths) {
        super(material);
        this.iconPaths = iconPaths;
        setBlockName(name);
        setHardness(hardness);
        setResistance(resistance);
        if (iconPaths.length == 0) {
            throw new IllegalArgumentException("Block " + name + " needs at least one icon path");
        }
    }

    @Override
    public void registerBlockIcons(IIconRegister register) {
        icons = new IIcon[iconPaths.length];
        for (int i = 0; i < iconPaths.length; i++) {
            icons[i] = register.registerIcon(MyMod.MODID + ":" + iconPaths[i]);
        }
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return icons[iconIndexFor(side, meta)];
    }

    protected int iconIndexFor(int side, int meta) {
        return 0;
    }
}
