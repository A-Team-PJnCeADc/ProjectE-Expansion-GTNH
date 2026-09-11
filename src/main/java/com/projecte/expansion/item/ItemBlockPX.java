package com.projecte.expansion.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import com.projecte.expansion.api.PXEmcValues;
import com.projecte.expansion.registry.PXCreativeTab;

public class ItemBlockPX extends ItemBlock {

    public ItemBlockPX(Block block) {
        super(block);
        setCreativeTab(PXCreativeTab.TAB);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> lines, boolean advanced) {
        PXEmcValues.addTooltip(stack, lines);
    }
}
