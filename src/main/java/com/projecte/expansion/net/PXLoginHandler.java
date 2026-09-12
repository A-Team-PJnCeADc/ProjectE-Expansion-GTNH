package com.projecte.expansion.net;

import net.minecraft.entity.player.EntityPlayerMP;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;

/**
 * 玩家登录时把服务端的精确 EMC 值同步给他。
 *
 * <p>
 * 登录时机在服务端完成初始化之后(自有求价引擎跑在 {@code FMLServerStartedEvent},AutoEMC-GTNH 也在
 * 那前后注册),所以此时表里已经有全部精确值;ProjectE 自己的同步包在这一刻只发 int,这里补上差量。
 */
public final class PXLoginHandler {

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            PXExactEmcSync.sendTo((EntityPlayerMP) event.player);
        }
    }
}
