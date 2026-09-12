package com.projecte.expansion.net;

import java.nio.charset.StandardCharsets;

import com.projecte.expansion.MyMod;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;

/**
 * S->C:一批精确 EMC 值(超过 ProjectE int 上限的那些)。
 *
 * <p>
 * 分片发送:一条最大 {@code FMLProxyPacket} 放不下三万多个值,所以按 {@link #MAX_ENTRIES} 分片、
 * 带上下标(index/total)便于日志核对与将来做去重。值用十进制文本传输(BigInteger 无上限)。
 */
public class PXExactEmcPKT implements IMessage {

    /** 每片的条目数上限(约 200 × 30 字节,远低于 32KB 的包上限) */
    public static final int MAX_ENTRIES = 200;

    public int index;
    public int total;
    public int[] itemIds;
    public int[] damages;
    public String[] values;

    public PXExactEmcPKT() {}

    public PXExactEmcPKT(int index, int total, int[] itemIds, int[] damages, String[] values) {
        this.index = index;
        this.total = total;
        this.itemIds = itemIds;
        this.damages = damages;
        this.values = values;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        index = buf.readInt();
        total = buf.readInt();
        int count = buf.readInt();
        itemIds = new int[count];
        damages = new int[count];
        values = new String[count];
        for (int i = 0; i < count; i++) {
            itemIds[i] = buf.readInt();
            damages[i] = buf.readInt();
            int length = buf.readInt();
            byte[] bytes = new byte[length];
            buf.readBytes(bytes);
            values[i] = new String(bytes, StandardCharsets.UTF_8);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(index);
        buf.writeInt(total);
        int count = itemIds == null ? 0 : itemIds.length;
        buf.writeInt(count);
        for (int i = 0; i < count; i++) {
            buf.writeInt(itemIds[i]);
            buf.writeInt(damages[i]);
            byte[] bytes = values[i].getBytes(StandardCharsets.UTF_8);
            buf.writeInt(bytes.length);
            buf.writeBytes(bytes);
        }
    }

    public static class Handler implements IMessageHandler<PXExactEmcPKT, IMessage> {

        @Override
        public IMessage onMessage(PXExactEmcPKT message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                // 网络包处理器运行在 Netty IO 线程:不能直接动表(渲染线程同时在读),
                // 交给代理排队,由客户端 tick 在主线程应用(与 ProjectE-Team 的做法一致)
                MyMod.proxy.handleExactEmc(message);
            }
            return null;
        }
    }
}
