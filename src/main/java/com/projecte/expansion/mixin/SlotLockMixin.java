package com.projecte.expansion.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import moze_intel.projecte.gameObjs.container.slots.transmutation.SlotLock;
import moze_intel.projecte.utils.Constants;

/**
 * 在 EMC 上限被解除后,保持转化桌锁定槽的行为正常。
 *
 * <p>
 * ProjectE 以 {@code Constants.TILE_MAX_EMC - (int) Math.ceil(inv.emc)} 计算剩余余量,
 * 玩家持有超过 2^30 EMC 时该值就会变为负数;此时把存储 EMC 的物品(Klein Star)
 * 放入锁定槽,反而会<em>抽取</em>约 1.07e9 的 EMC,而不是注入它。
 * 重定向 {@code Math.ceil} 调用后,这类玩家的余量会夹取(clamp)到 ProjectE 自身的
 * {@code TILE_MAX_EMC},于是物品 EMC 仍能完整转移。
 *
 * <p>
 * {@code putStack} 是 Minecraft 的 {@code Slot.func_75215_d};{@code remap = false} 加上
 * 两个名称可保证补丁可解析,{@code require = 0} 则让名称不匹配也无害。
 */
@Mixin(SlotLock.class)
public abstract class SlotLockMixin {

    @Redirect(
        method = { "putStack", "func_75215_d" },
        at = @At(value = "INVOKE", target = "Ljava/lang/Math;ceil(D)D"),
        require = 0,
        remap = false)
    private static double projectexpansiongtnh$clampHeadroom(double playerEmc) {
        return playerEmc >= (double) Constants.TILE_MAX_EMC ? 0D : playerEmc;
    }
}
