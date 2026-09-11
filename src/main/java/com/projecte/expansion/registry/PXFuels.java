package com.projecte.expansion.registry;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.projecte.expansion.util.Fuel;

import cpw.mods.fml.common.IFuelHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import moze_intel.projecte.utils.Constants;

/**
 * 各色燃料的熔炉燃烧时间(与上游语义一致:某色燃料的燃烧时间等同永恒燃料,
 * 其方块则等同 9 个该燃料)。
 */
public final class PXFuels {

    private PXFuels() {}

    public static void register() {
        GameRegistry.registerFuelHandler(new FuelHandler());
    }

    private static class FuelHandler implements IFuelHandler {

        @Override
        public int getBurnTime(ItemStack fuel) {
            for (Fuel f : Fuel.VALUES) {
                if (!f.hasOwnItem()) continue;
                Item item = PXItems.FUEL_ITEMS.get(f);
                Item blockItem = PXItems.FUEL_BLOCKS.get(f) == null ? null
                    : Item.getItemFromBlock(PXItems.FUEL_BLOCKS.get(f));
                if (item != null && fuel.getItem() == item) {
                    return Constants.AETERNALIS_BUR_TIME;
                }
                if (blockItem != null && fuel.getItem() == blockItem) {
                    return Constants.AETERNALIS_BUR_TIME * 9;
                }
            }
            return 0;
        }
    }
}
