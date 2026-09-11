package com.projecte.expansion.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.projecte.expansion.registry.PXPatchLog;

import moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory;

/**
 * 解除 ProjectE 对转化桌 EMC 的上限。
 *
 * <p>
 * {@code Constants.TILE_MAX_EMC} 为 2^30(1073741824),而 {@code addEmc} 会硬性夹取
 * (clamp)到该值,因此即便使用精确值,转化桌也永远无法超过约 1.07e9。存储的值本身是
 * double({@code TransmutationProps.transmutationEmc},按 double 持久化并同步),因此没有
 * 其他因素会限制它。{@code addEmc} 和 {@code hasMaxedEmc} 都是 ProjectE 自己的方法,
 * 其名称在反混淆后保持不变,因此 {@code remap = false} 是正确的。
 */
@Mixin(TransmutationInventory.class)
public abstract class TransmutationInventoryMixin {

    /** Double.MAX_VALUE 让转化桌保持纯 double 存储;不再有任何缩窄。 */
    private static final double PX_MAX_EMC = Double.MAX_VALUE;

    @Shadow(remap = false)
    public double emc;

    @Inject(method = "addEmc", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void projectexpansiongtnh$raiseCeiling(double value, CallbackInfo ci) {
        PXPatchLog.mark("player EMC ceiling lifted (was ProjectE 2^30)");
        double result = emc + value;
        emc = (Double.isNaN(result) || result < 0D) ? 0D : Math.min(result, PX_MAX_EMC);
        ci.cancel();
    }

    @Inject(method = "hasMaxedEmc", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void projectexpansiongtnh$raiseCeilingCheck(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(emc >= PX_MAX_EMC);
    }
}
