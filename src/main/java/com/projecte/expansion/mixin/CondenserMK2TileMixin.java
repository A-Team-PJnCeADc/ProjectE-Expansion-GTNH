package com.projecte.expansion.mixin;

import java.math.BigInteger;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.projecte.expansion.registry.PXEmcTable;
import com.projecte.expansion.registry.PXPatchLog;

import moze_intel.projecte.api.tile.TileEmcBase;
import moze_intel.projecte.gameObjs.tiles.CondenserMK2Tile;
import moze_intel.projecte.gameObjs.tiles.CondenserTile;
import moze_intel.projecte.utils.EMCHelper;

/**
 * ProjectE 能量冷凝器 MK2 的运行时补丁(无需重新编译 ProjectE):烧掉本 mod 的物品
 * 会按精确的 BigInteger EMC 计入。见 {@link CondenserTileMixin}。
 */
@Mixin(CondenserMK2Tile.class)
public abstract class CondenserMK2TileMixin {

    private static final int INPUT_SLOTS_LOWER = 1;
    private static final int INPUT_SLOTS_UPPER = 42;

    @Inject(method = "condense", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void projectexpansiongtnh$exactPrices(CallbackInfo ci) {
        PXPatchLog.mark("condenser mk2 (burn exact EMC)");
        CondenserTileInvoker self = (CondenserTileInvoker) (Object) this;
        ItemStack[] inventory = self.pxInventory();
        int requiredEmc = ((CondenserTile) (Object) this).requiredEmc;

        while (self.pxHasSpace() && ((TileEmcBase) (Object) this).getStoredEmc() >= requiredEmc) {
            self.pxPushStack();
            ((TileEmcBaseInvoker) (Object) this).pxRemoveEMC(requiredEmc);
        }

        if (self.pxHasSpace()) {
            for (int i = INPUT_SLOTS_LOWER; i <= INPUT_SLOTS_UPPER; i++) {
                ItemStack stack = inventory[i];

                if (stack == null) {
                    continue;
                }

                ((TileEmcBaseInvoker) (Object) this).pxAddEMC(priceOf(stack) * stack.stackSize);
                inventory[i] = null;
                break;
            }
        }

        ci.cancel();
    }

    /** 本 mod 物品用精确的 BigInteger 价格,其余用 ProjectE 的 int 值。 */
    private static double priceOf(ItemStack stack) {
        BigInteger exact = PXEmcTable.get(stack);
        return PXEmcTable.toPeDouble(exact != null ? exact : BigInteger.valueOf(EMCHelper.getEmcValue(stack)));
    }
}
