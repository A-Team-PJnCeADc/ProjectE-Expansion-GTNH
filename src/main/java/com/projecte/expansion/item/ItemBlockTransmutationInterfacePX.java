package com.projecte.expansion.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import com.projecte.expansion.api.PXEmcValues;

/** 转化接口的物品形态:精确 EMC 行 + 方块用法说明(1.7.10 的 Block 没有 addInformation)。 */
public class ItemBlockTransmutationInterfacePX extends ItemBlockPX {

    public ItemBlockTransmutationInterfacePX(Block block) {
        super(block);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> lines, boolean advanced) {
        for (int i = 1; i <= 3; i++) {
            lines.add(StatCollector.translateToLocal("item.projectexpansiongtnh.transmutation_interface.tooltip." + i));
        }
        PXEmcValues.addTooltip(stack, lines);
    }
}
