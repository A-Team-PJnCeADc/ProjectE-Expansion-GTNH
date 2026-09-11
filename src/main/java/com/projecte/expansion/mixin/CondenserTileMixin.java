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
import moze_intel.projecte.gameObjs.tiles.CondenserTile;
import moze_intel.projecte.utils.EMCHelper;

/**
 * 对 ProjectE 冷凝器 MKI 的运行时补丁（无需重新编译 ProjectE）：烧毁本 mod 的物品时会加入其
 * 精确的 BigInteger EMC，而不是受 int 上限限制的 {@code EMCHelper.getEmcValue}。冷凝器的
 * 存储量是 double，因此超过 Integer.MAX_VALUE 的数值没有问题。
 *
 * <p>
 * 注入到 {@code condense()} 的开头并取消原方法（而非使用 {@code @Overwrite}），配合
 * {@code require = 0}，使名称不匹配时只是禁用该补丁而不会崩溃。
 */
@Mixin(CondenserTile.class)
public abstract class CondenserTileMixin {

    @Inject(method = "condense", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void projectexpansiongtnh$exactPrices(CallbackInfo ci) {
        PXPatchLog.mark("condenser mk1 (burn exact EMC)");
        CondenserTileInvoker self = (CondenserTileInvoker) (Object) this;
        ItemStack[] inventory = self.pxInventory();
        CondenserTile target = (CondenserTile) (Object) this;

        for (int i = 1; i < 92; i++) {
            ItemStack stack = target.getStackInSlot(i);

            if (stack == null || self.pxIsStackEqualToLock(stack)) {
                continue;
            }

            if (stack.stackSize <= 0) {
                inventory[i] = null;
                continue;
            }

            ItemStack burned = stack.copy();
            burned.stackSize = 1;
            target.decrStackSize(i, 1);
            ((TileEmcBaseInvoker) (Object) this).pxAddEMC(priceOf(burned));
            break;
        }

        if (((TileEmcBase) (Object) this).getStoredEmc() >= target.requiredEmc && self.pxHasSpace()) {
            ((TileEmcBaseInvoker) (Object) this).pxRemoveEMC(target.requiredEmc);
            self.pxPushStack();
        }

        ci.cancel();
    }

    /** 本 mod 的物品返回精确的 BigInteger 价格，其余情况返回 ProjectE 的 int 值。 */
    private static double priceOf(ItemStack stack) {
        BigInteger exact = PXEmcTable.get(stack);
        return PXEmcTable.toPeDouble(exact != null ? exact : BigInteger.valueOf(EMCHelper.getEmcValue(stack)));
    }
}
