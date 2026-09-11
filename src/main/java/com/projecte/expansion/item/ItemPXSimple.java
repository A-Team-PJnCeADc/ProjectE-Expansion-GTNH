package com.projecte.expansion.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.projecte.expansion.MyMod;
import com.projecte.expansion.api.PXEmcValues;
import com.projecte.expansion.registry.PXCreativeTab;

/**
 * 带静态图标和可选稀有度的简单物品。图标路径相对于
 * assets/<modid>/textures/items/。
 */
public class ItemPXSimple extends Item {

    protected final EnumRarity rarity;

    public ItemPXSimple(String name, String iconPath) {
        this(name, iconPath, EnumRarity.common);
    }

    public ItemPXSimple(String name, String iconPath, EnumRarity rarity) {
        this.rarity = rarity;
        setUnlocalizedName(name);
        setTextureName(MyMod.MODID + ":" + iconPath);
        setCreativeTab(PXCreativeTab.TAB);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return rarity;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> lines, boolean advanced) {
        // ProjectE 自身的 EMC 表经过 int 夹取(clamp),因此这里显示精确的 BigInteger 值
        PXEmcValues.addTooltip(stack, lines);
    }
}
