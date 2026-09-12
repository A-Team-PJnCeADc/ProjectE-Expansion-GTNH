package com.projecte.expansion.net;

import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;

import com.projecte.expansion.MyMod;
import com.projecte.expansion.registry.PXEmcTable;

/**
 * 把服务端的精确 EMC 值同步给客户端(玩家登录时发一次)。
 *
 * <p>
 * 见 {@link PXNetwork} 的说明:专用服务器上客户端没有精确表,ProjectE 的包又只能带 int,
 * 所以客户端显示的价格会是 2147483647。这里只发超过 int 上限的条目(通常几千条,分十几片)。
 */
public final class PXExactEmcSync {

    private PXExactEmcSync() {}

    /** 把当前精确表里"超过 ProjectE int 上限"的条目分片发给该玩家。 */
    public static void sendTo(EntityPlayerMP player) {
        if (player == null) {
            return;
        }
        List<PXEmcTable.SyncEntry> entries = PXEmcTable.entriesForSync();
        if (entries.isEmpty()) {
            return;
        }
        int chunkSize = PXExactEmcPKT.MAX_ENTRIES;
        int total = (entries.size() + chunkSize - 1) / chunkSize;
        for (int chunk = 0; chunk < total; chunk++) {
            int from = chunk * chunkSize;
            int to = Math.min(from + chunkSize, entries.size());
            int count = to - from;
            int[] itemIds = new int[count];
            int[] damages = new int[count];
            String[] values = new String[count];
            for (int i = 0; i < count; i++) {
                PXEmcTable.SyncEntry entry = entries.get(from + i);
                itemIds[i] = entry.itemId;
                damages[i] = entry.damage;
                values[i] = entry.value;
            }
            PXNetwork.sendTo(player, new PXExactEmcPKT(chunk, total, itemIds, damages, values));
        }
        MyMod.LOG.info(
            "Sent {} exact EMC values ({} packet(s)) to {}.",
            entries.size(),
            total,
            player.getCommandSenderName());
    }
}
