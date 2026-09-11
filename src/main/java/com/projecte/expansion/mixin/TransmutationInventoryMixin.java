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

    /** HEAD 记录调用前的值;这里**不能取消**方法,原因见下方 TAIL 注入的注释。 */
    private double pxEmcBeforeAdd;
    private boolean pxEmcCaptured;

    @Inject(method = "addEmc", at = @At("HEAD"), require = 0, remap = false)
    private void projectexpansiongtnh$captureBeforeAdd(double value, CallbackInfo ci) {
        PXPatchLog.mark("player EMC addEmc pre-capture (HEAD, non-cancellable)");
        pxEmcBeforeAdd = emc;
        pxEmcCaptured = true;
    }

    /**
     * TAIL 修正被夹取的结果。
     *
     * <p>
     * 这里刻意**不用** {@code HEAD + cancel} 的写法:ProjectE-Team 在 {@code addEmc} 的 TAIL
     * 注入团队池转发({@code TeamManager.onEmcAdd}),而 HEAD 取消会让原方法体连同它的 RETURN
     * 一起不执行,TAIL 注入点因此永远不会跑 —— 症状是团队模式下(共享 EMC 开着)卖东西时
     * "EMC 不进团队池、转化桌仍显示团队池原值(0)",而且卖掉的东西等于凭空消失。
     * 所以让原逻辑照常执行,只在最后把正确结果覆盖回去;两个 TAIL 处理器互不依赖
     * (对方只用参数 {@code value}),顺序无关。
     */
    @Inject(method = "addEmc", at = @At("TAIL"), require = 0, remap = false)
    private void projectexpansiongtnh$raiseCeiling(double value, CallbackInfo ci) {
        PXPatchLog.mark("player EMC ceiling lifted (was ProjectE 2^30)");
        double base = pxEmcCaptured ? pxEmcBeforeAdd : emc - value;
        pxEmcCaptured = false;
        double result = base + value;
        // 负数/NaN/无穷(unset 的 Infinity 或量级溢出)一律收敛成合法值,避免余额度变成 Infinity
        emc = (Double.isNaN(result) || Double.isInfinite(result) || result < 0D) ? Double.MAX_VALUE
            : Math.min(result, PX_MAX_EMC);
    }

    @Inject(method = "hasMaxedEmc", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void projectexpansiongtnh$raiseCeilingCheck(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(emc >= PX_MAX_EMC);
    }
}
