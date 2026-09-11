package com.projecte.expansion.mixin;

import java.math.BigInteger;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.projecte.expansion.registry.PXEmcTable;
import com.projecte.expansion.registry.PXPatchLog;

import moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory;
import moze_intel.projecte.gameObjs.container.slots.transmutation.SlotConsume;
import moze_intel.projecte.utils.EMCHelper;

/**
 * 转化桌消耗槽(ProjectE 的 {@code SlotConsume})的运行时补丁:放入其中的物品按精确的
 * BigInteger EMC 值计入,而不是 ProjectE 以 int 封顶的 {@code EMCHelper.getEmcValue}。
 * 这是最常见的"向转化桌出售"路径。
 *
 * <p>
 * {@code putStack} 就是 Minecraft 的 {@code Slot.putStack}(发布版 jar 中为 {@code func_75215_d}),
 * 因此两个名字都列出并带 {@code remap = false};{@code require = 0} 使名称不匹配时也不会出错。
 */
@Mixin(SlotConsume.class)
public abstract class SlotConsumeMixin {

    @Shadow(remap = false)
    private TransmutationInventory inv;

    @Inject(method = { "putStack", "func_75215_d" }, at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void projectexpansiongtnh$exactPrices(ItemStack stack, CallbackInfo ci) {
        PXPatchLog.mark("transmutation tablet consume slot (sell)");
        if (stack == null) {
            return;
        }

        ItemStack cache = stack.copy();
        double toAdd = 0;

        while (!inv.hasMaxedEmc() && stack.stackSize > 0) {
            toAdd += priceOf(stack);
            stack.stackSize--;
        }

        inv.addEmc(toAdd);
        // ProjectE 在此调用 this.onSlotChanged();即 Slot.onSlotChanged() -> TransmutationInventory
        // .markDirty(),而 ProjectE 把它实现为空操作(no-op),所以省略它不会丢失任何东西
        // (而且 @Shadow 一个由父类声明的方法会导致 mixin APPLY 阶段失败)。
        inv.handleKnowledge(cache);

        ci.cancel();
    }

    /** 本 mod 物品用精确的 BigInteger 价格,其余用 ProjectE 的 int 值。 */
    private static double priceOf(ItemStack stack) {
        BigInteger exact = PXEmcTable.get(stack);
        return PXEmcTable.toPeDouble(exact != null ? exact : BigInteger.valueOf(EMCHelper.getEmcValue(stack)));
    }
}
