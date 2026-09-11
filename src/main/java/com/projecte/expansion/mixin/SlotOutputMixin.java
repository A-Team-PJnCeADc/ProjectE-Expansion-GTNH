package com.projecte.expansion.mixin;

import java.math.BigInteger;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.projecte.expansion.registry.PXEmcTable;
import com.projecte.expansion.registry.PXPatchLog;

import moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory;
import moze_intel.projecte.gameObjs.container.slots.transmutation.SlotOutput;
import moze_intel.projecte.utils.EMCHelper;

/**
 * 对转化桌输出槽（ProjectE 的 {@code SlotOutput}）的运行时补丁：取出物品时按其精确的
 * BigInteger EMC 值扣费，而不是使用 ProjectE 受 int 上限限制的 {@code EMCHelper.getEmcValue}。
 * 这就是点击购买的路径。
 *
 * <p>
 * {@code decrStackSize} 即 Minecraft 的 {@code Slot.decrStackSize}（正式发布版 jar 中为
 * {@code func_75209_a}），因此两个名称都以 {@code remap = false} 列出；{@code require = 0}
 * 可使名称不匹配时不产生危害。
 */
@Mixin(SlotOutput.class)
public abstract class SlotOutputMixin {

    @Shadow(remap = false)
    private TransmutationInventory inv;

    @Inject(
        method = { "decrStackSize", "func_75209_a" },
        at = @At("HEAD"),
        cancellable = true,
        require = 0,
        remap = false)
    private void projectexpansiongtnh$exactPrices(int amount, CallbackInfoReturnable<ItemStack> cir) {
        PXPatchLog.mark("transmutation tablet output slots (buy)");
        ItemStack stack = ((Slot) (Object) this).getStack()
            .copy();
        stack.stackSize = amount;
        double cost = priceOf(stack) * amount;

        if (cost > inv.emc) {
            // 请求的 EMC 超过可用量：Container 期望的是大小为 0 的物品堆，而不是 null
            stack.stackSize = 0;
            cir.setReturnValue(stack);
            return;
        }

        inv.removeEmc(cost);
        inv.checkForUpdates();

        cir.setReturnValue(stack);
    }

    /** 本 mod 的物品返回精确的 BigInteger 价格，其余情况返回 ProjectE 的 int 值。 */
    private static double priceOf(ItemStack stack) {
        BigInteger exact = PXEmcTable.get(stack);
        return PXEmcTable.toPeDouble(exact != null ? exact : BigInteger.valueOf(EMCHelper.getEmcValue(stack)));
    }
}
