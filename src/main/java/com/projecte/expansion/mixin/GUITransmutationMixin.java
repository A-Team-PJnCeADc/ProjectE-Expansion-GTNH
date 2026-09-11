package com.projecte.expansion.mixin;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.StatCollector;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.projecte.expansion.registry.PXEmcTable;
import com.projecte.expansion.registry.PXPatchLog;
import com.projecte.expansion.util.EMCFormatPX;

import moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory;
import moze_intel.projecte.gameObjs.gui.GUITransmutation;

/**
 * 转化桌 GUI 用 {@code String.format(..., (int) inv.emc)} 打印 EMC:把 double 收窄成 int 会在
 * 2147483647 处饱和,所以无论转化桌里存了多少 EMC,那一行都只显示 2,147,483,647。
 * 这里把那一个局部变量(该方法存储的唯一 String 局部变量)换成我们的缩写值
 * (例如 {@code 2.72Qi}),同时完全不动 ProjectE 的布局。
 *
 * <p>
 * 这里使用带 String 处理器的 {@code @ModifyVariable},而不是重定向 {@code String.format} 调用:
 * 那个目标是静态方法,而 Mixin 只会把非静态处理器绑定到实例方法上,所以该重定向静默失效
 * (它必须保持无害:{@code require = 0})。
 *
 * <p>
 * {@code drawGuiContainerForegroundLayer} 就是 Minecraft 的 {@code GuiContainer.func_146979_b}
 * (已用 javap 在发布版 PE jar 上核实,别凭记忆猜 SRG 名),因此两个名字都要列出并带
 * {@code remap = false}。
 */
@Mixin(GUITransmutation.class)
public abstract class GUITransmutationMixin {

    @Shadow(remap = false)
    TransmutationInventory inv;

    @ModifyVariable(
        method = { "drawGuiContainerForegroundLayer", "func_146979_b" },
        at = @At("STORE"),
        require = 0,
        remap = false)
    private String projectexpansiongtnh$emcLine(String original) {
        PXPatchLog.mark("transmutation tablet EMC display (ModifyVariable)");
        return emcLine();
    }

    /**
     * 第二道保险:如果 {@code @ModifyVariable} 在这个 Mixin 版本/环境下没绑定上,直接重定向渲染那一行
     * EMC 文本的 {@code FontRenderer.drawString} 调用(目标用发布版 jar 里的 SRG 名
     * {@code func_78276_b},已在 mcp-srg 映射上核实)。只改以 {@code EMC: } 开头的行,其它行原样转发。
     */
    @Redirect(
        method = { "drawGuiContainerForegroundLayer", "func_146979_b" },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/FontRenderer;func_78276_b(Ljava/lang/String;III)I"),
        require = 0,
        remap = false)
    private int projectexpansiongtnh$emcLineDraw(FontRenderer font, String text, int x, int y, int color) {
        String prefix = StatCollector.translateToLocal("pe.emc.emc_tooltip_prefix");
        if (text != null && text.startsWith(prefix)) {
            PXPatchLog.mark("transmutation tablet EMC display (drawString redirect)");
            return font.drawString(emcLine(), x, y, color);
        }
        return font.drawString(text, x, y, color);
    }

    /** 转化桌 EMC 行文本:用我们的缩写值(例如 2.72Qi),而不是被 (int) 截断的十进制数。 */
    private String emcLine() {
        return StatCollector.translateToLocal("pe.emc.emc_tooltip_prefix") + " "
            + EMCFormatPX.format(PXEmcTable.fromPe(inv.emc));
    }
}
