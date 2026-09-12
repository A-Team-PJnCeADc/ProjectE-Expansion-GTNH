package com.projecte.expansion.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

import com.projecte.expansion.registry.PXCreativeTab;

/**
 * 本 mod 方块对应的物品形态。
 *
 * <p>
 * 这里**不再**追加"精确 EMC:"行:ProjectE 自己会给有价物品加 {@code EMC: 缩写 (精确值)}
 * 一行({@link com.projecte.expansion.mixin.ToolTipEventMixin} 负责补精确值),
 * 再加一行就是同一个数值显示两遍。
 */
public class ItemBlockPX extends ItemBlock {

    public ItemBlockPX(Block block) {
        super(block);
        setCreativeTab(PXCreativeTab.TAB);
    }
}
