package com.projecte.expansion.mixin;

import net.minecraft.command.ICommandSender;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.projecte.expansion.registry.PXEmcRefresher;
import com.projecte.expansion.registry.PXPatchLog;

import moze_intel.projecte.network.commands.ReloadEmcCMD;

/**
 * 给 ProjectE 的 {@code /projecte reloadEMC} 接一条尾巴:它重算完值表之后,把本模组的精确值表
 * 重新推给所有在线玩家。
 *
 * <p>
 * 为什么必须接在 PE 的命令上:PE 的手册式提示是"刷新完成后…使用 /projecte reloadEMC",玩家照做时
 * 走的是这条命令;PE 只重发它那张 int 值表,本模组的 BigInteger 精确表只在登录时发过,于是客户端
 * 拿着旧价 —— 表现就是"重载了,客户端没变化"。接上之后两条命令(PE 的和
 * {@code /projectexpansion reloademc})都能让客户端拿到新值。
 *
 * <p>
 * 方法名同时列 {@code processCommand} 与发布版里的 SRG 名 {@code func_71515_b},并带
 * {@code require = 0}:名称不匹配时静默跳过,不会因为映射差异炸掉加载。
 */
@Mixin(ReloadEmcCMD.class)
public abstract class ReloadEmcCommandMixin {

    @Inject(method = { "processCommand", "func_71515_b" }, at = @At("TAIL"), require = 0, remap = false)
    private void projectexpansiongtnh$resendExactEmc(ICommandSender sender, String[] params, CallbackInfo ci) {
        PXPatchLog.mark("projecte reloadEMC -> resend exact EMC values to online players");
        PXEmcRefresher.resendExactToAll();
    }
}
