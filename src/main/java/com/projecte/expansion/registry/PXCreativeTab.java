package com.projecte.expansion.registry;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** 单个创造标签页;其内容按与上游一致的固定顺序填充。 */
public class PXCreativeTab extends CreativeTabs {

    public static final PXCreativeTab TAB = new PXCreativeTab();

    private static final List<ItemStack> STACKS = new ArrayList<>();

    public PXCreativeTab() {
        super("projectexpansiongtnh");
    }

    public static void add(Item item) {
        STACKS.add(new ItemStack(item));
    }

    @Override
    public Item getTabIconItem() {
        return PXItems.finalStar;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void displayAllReleventItems(List list) {
        list.addAll(STACKS);
    }
}
