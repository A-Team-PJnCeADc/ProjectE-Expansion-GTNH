package com.projecte.expansion.mixin;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import moze_intel.projecte.gameObjs.tiles.CondenserTile;

/**
 * 访问 ProjectE 冷凝器的 accessor:它的 inventory 字段加上那些 protected 辅助方法,这样冷凝器的
 * mixin 就能直接使用它们,而不必 shadow 继承来的成员(对于目标类之外声明的成员,mixin 会拒绝
 * shadow)。
 */
@Mixin(CondenserTile.class)
public interface CondenserTileInvoker {

    // ProjectE 自有成员(发布版 jar 中未被混淆),必须 remap = false,否则注解处理器找不到混淆映射
    @Accessor(value = "inventory", remap = false)
    ItemStack[] pxInventory();

    @Invoker(value = "hasSpace", remap = false)
    boolean pxHasSpace();

    @Invoker(value = "pushStack", remap = false)
    void pxPushStack();

    @Invoker(value = "isStackEqualToLock", remap = false)
    boolean pxIsStackEqualToLock(ItemStack stack);
}
