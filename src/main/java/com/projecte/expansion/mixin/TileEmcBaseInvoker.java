package com.projecte.expansion.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import moze_intel.projecte.api.tile.TileEmcBase;

/**
 * 进入 ProjectE EMC 基础 TE 的访问器,使机器相关的 mixin 能调用其 protected 的
 * {@code addEMC}/{@code removeEMC}(mixin 无法直接 @Shadow 继承来的 protected 方法)。
 */
@Mixin(TileEmcBase.class)
public interface TileEmcBaseInvoker {

    // ProjectE 自有成员(非 MC 方法,未被混淆),remap = false 才能消掉注解处理器的混淆映射警告
    @Invoker(value = "addEMC", remap = false)
    void pxAddEMC(double amount);

    @Invoker(value = "removeEMC", remap = false)
    void pxRemoveEMC(double amount);
}
