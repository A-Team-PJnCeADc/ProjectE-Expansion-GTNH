package com.projecte.expansion.mixin;

import java.math.BigInteger;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.projecte.expansion.registry.PXEmcTable;
import com.projecte.expansion.registry.PXPatchLog;

import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.gameObjs.container.TransmutationContainer;
import moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.ItemHelper;

/**
 * 对 ProjectE 转化桌的运行时补丁(mixin patch)(无需重新编译 ProjectE):Shift 点击时
 * 按本模组物品的精确 BigInteger EMC 值扣费 / 收费,而不再使用 ProjectE 经 int
 * 夹取(clamp)的 {@code EMCHelper.getEmcValue}。转化桌自身的存储
 * ({@code TransmutationInventory.emc})是 double,因此远超 Integer.MAX_VALUE 的价格也能正常工作。
 *
 * <p>
 * <b>目标名称:</b>此处对应 Minecraft 的 {@code Container.transferStackInSlot};发布版
 * mod jar 携带 SRG 名({@code func_82846_b}),开发工作区使用 MCP 名,因此两个别名
 * 都列出并带 {@code remap = false} — 这样解析就永不依赖 refmap。
 *
 * <p>
 * <b>安全性:</b>HEAD 注入并 cancel + {@code require = 0},因此名称不匹配只会让补丁
 * 不生效,而不会使游戏崩溃。对 ProjectE 逻辑的任何其他改动都必须在此同步;
 * {@code SlotConsumeMixin} / {@code SlotOutputMixin} 覆盖另外两处求价点。
 */
@Mixin(TransmutationContainer.class)
public abstract class TransmutationContainerMixin {

    @Shadow(remap = false)
    public TransmutationInventory transmutationInventory;

    @Inject(
        method = { "transferStackInSlot", "func_82846_b" },
        at = @At("HEAD"),
        cancellable = true,
        require = 0,
        remap = false)
    private void projectexpansiongtnh$exactPrices(EntityPlayer player, int slotIndex,
        CallbackInfoReturnable<ItemStack> cir) {
        PXPatchLog.mark("transmutation tablet shift-click (sell/buy)");
        Slot slot = ((Container) (Object) this).getSlot(slotIndex);

        if (slot == null || !slot.getHasStack()) {
            cir.setReturnValue(null);
            return;
        }

        ItemStack stack = slot.getStack();
        ItemStack newStack = stack.copy();

        if (slotIndex <= 7) // 输入槽:无需处理
        {
            cir.setReturnValue(null);
            return;
        } else if (slotIndex >= 10 && slotIndex <= 25) // 输出槽:购买
        {
            double price = priceOf(newStack);
            int stackSize = 0;

            while (transmutationInventory.emc >= price && stackSize < newStack.getMaxStackSize()
                && ItemHelper.hasSpace(player.inventory.mainInventory, newStack)) {
                transmutationInventory.removeEmc(price);
                ItemHelper.pushStackInInv(player.inventory, ItemHelper.getNormalizedStack(newStack));
                stackSize++;
            }

            transmutationInventory.updateOutputs();
        } else if (slotIndex >= 26) // 玩家物品栏 / 遗忘槽:出售
        {
            double price = priceOf(stack);

            if (price == 0 && stack.getItem() != ObjHandler.tome) {
                cir.setReturnValue(null);
                return;
            }

            while (!transmutationInventory.hasMaxedEmc() && stack.stackSize > 0) {
                transmutationInventory.addEmc(price);
                --stack.stackSize;
            }

            transmutationInventory.handleKnowledge(newStack);

            if (stack.stackSize == 0) {
                slot.putStack(null);
            }
        }

        cir.setReturnValue(null);
    }

    /** 本模组物品返回精确的 BigInteger 价格,否则返回 ProjectE 的 int 值。 */
    private static double priceOf(ItemStack stack) {
        BigInteger exact = PXEmcTable.get(stack);
        return PXEmcTable.toPeDouble(exact != null ? exact : BigInteger.valueOf(EMCHelper.getEmcValue(stack)));
    }
}
