package com.projecte.expansion.net;

import net.minecraft.entity.player.EntityPlayerMP;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

/**
 * 本模组的网络频道:只用于把**精确 EMC 值**同步给客户端(方向 S->C)。
 *
 * <p>
 * 为什么需要它:本模组的精确值表是在服务端生成的(自有求价引擎在 {@code FMLServerStartedEvent} 跑,
 * AutoEMC-GTNH 也在服务端注册),而 ProjectE 自己的同步包只能携带 int(它的表是
 * {@code Map<SimpleStack, Integer>})。因此在专用服务器上,客户端的精确表是空的,任何客户端显示
 * (物品提示、创造物品栏、转化桌里的价格)都只能读到被夹取的 {@code 2147483647}。
 * 这里把超过 int 上限的条目单独传给客户端,补上那一块。
 */
public final class PXNetwork {

    public static final String CHANNEL_NAME = "PEXP";

    private static final SimpleNetworkWrapper HANDLER = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL_NAME);

    private PXNetwork() {}

    /** 在 preInit 注册消息(仅 S->C) */
    public static void register() {
        HANDLER.registerMessage(PXExactEmcPKT.Handler.class, PXExactEmcPKT.class, 0, Side.CLIENT);
    }

    /** 向单个玩家发送(玩家未连接/已断开时静默跳过) */
    public static void sendTo(EntityPlayerMP player, IMessage message) {
        if (player != null && player.playerNetServerHandler != null) {
            HANDLER.sendTo(message, player);
        }
    }
}
