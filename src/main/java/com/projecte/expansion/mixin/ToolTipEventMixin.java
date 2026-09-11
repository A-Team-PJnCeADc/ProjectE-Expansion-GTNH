package com.projecte.expansion.mixin;

import java.math.BigInteger;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.projecte.expansion.registry.PXEmcTable;
import com.projecte.expansion.registry.PXPatchLog;
import com.projecte.expansion.util.EMCFormatPX;

import moze_intel.projecte.events.ToolTipEvent;

/**
 * 物品提示(创造物品栏、背包、NEI 面板里悬停物品时看到的那一行)的精确 EMC 显示。
 *
 * <p>
 * ProjectE 的 {@code ToolTipEvent.tTipEvent(ItemTooltipEvent)} 会给有价的物品加一行
 * {@code EMC: <值>}(以及整叠的 {@code Stack EMC: <值×数量>}),而它读的是
 * {@code EMCHelper.getEmcValue}(int,上限 2147483647)。因此高价物品在提示里永远显示
 * {@code 2,147,483,647}。这里在 PE 写完提示后把那一行的数字换成我们的精确值
 * ({@code 2.72Qi (2720037735727963008)}),只在精确值确实超过 ProjectE 的 int 上限时替换,
 * 其余情况保持 PE 原样(避免把正常物品的提示搞乱)。
 *
 * <p>
 * 只能装在客户端({@link ItemTooltipEvent} 是纯客户端的 Forge 事件),因此本 mixin 写在
 * mixin 配置的 {@code client} 列表里。
 */
@Mixin(ToolTipEvent.class)
public abstract class ToolTipEventMixin {

    @Inject(method = "tTipEvent", at = @At("TAIL"), require = 0, remap = false)
    private void projectexpansiongtnh$exactEmcLine(ItemTooltipEvent event, CallbackInfo ci) {
        if (event == null) {
            return;
        }
        ItemStack stack = event.itemStack;
        List<String> lines = event.toolTip;
        if (stack == null || lines == null || lines.isEmpty()) {
            return;
        }
        BigInteger exact = PXEmcTable.get(stack);
        if (exact == null || exact.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0) {
            return; // 没超上限:PE 显示的就是精确值,不用动
        }

        String emcPrefix = StatCollector.translateToLocal("pe.emc.emc_tooltip_prefix");
        String stackPrefix = StatCollector.translateToLocal("pe.emc.stackemc_tooltip_prefix");
        boolean patched = false;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line == null) {
                continue;
            }
            String bare = stripColorCodes(line);
            String prefix = null;
            boolean wholeStack = false;
            if (bare.startsWith(stackPrefix)) {
                prefix = stackPrefix;
                wholeStack = true;
            } else if (bare.startsWith(emcPrefix)) {
                prefix = emcPrefix;
            }
            if (prefix == null) {
                continue;
            }
            // 整叠一行要乘上数量,和 ProjectE 的语义一致
            BigInteger shown = wholeStack ? exact.multiply(BigInteger.valueOf(Math.max(1, stack.stackSize))) : exact;
            lines.set(i, prefix + " " + EMCFormatPX.format(shown) + " (" + shown + ")");
            patched = true;
        }
        if (patched) {
            PXPatchLog.mark("item tooltip exact EMC (PE ToolTipEvent)");
        }
    }

    /** 去掉 Minecraft 的颜色代码(§x),便于比对语言文件的键值前缀。 */
    private static String stripColorCodes(String text) {
        if (text.indexOf('\u00a7') < 0) {
            return text;
        }
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\u00a7') {
                i++; // 跳过颜色代码本身
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
