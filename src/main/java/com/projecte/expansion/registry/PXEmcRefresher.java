package com.projecte.expansion.registry;

import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import com.projecte.expansion.net.PXExactEmcSync;

/**
 * 服务端"重载 EMC"后的收尾:重算本模组的精确值,并把精确值表重新推给所有在线玩家。
 *
 * <p>
 * 为什么需要它:PE 自己的 {@code /projecte reloadEMC} 只重发它那张 **int** 值表
 * ({@code PacketHandler.sendFragmentedEmcPacketToAll}),本模组的精确表(BigInteger,超过 int 上限的
 * 那些)只在**登录时**发过({@code PXExactEmcSync})。所以重载之后客户端拿着的还是旧值 —— 命令提示
 * "刷新完成后请重启游戏或使用 /projecte reloadEMC",但照做了客户端也不会变,必须重登。
 *
 * <p>
 * 两条入口都接到这里:{@link com.projecte.expansion.command.CommandPXReloadEmc}
 * ({@code /projectexpansion reloademc})与 PE 那条命令的 mixin 尾巴
 * ({@link com.projecte.expansion.mixin.ReloadEmcCommandMixin})。
 */
public final class PXEmcRefresher {

    private PXEmcRefresher() {}

    /**
     * 重算本模组的精确值并推给所有在线玩家(PE 的 int 值表由调用方自己重发)。
     *
     * @return 重算 + 推送耗时(毫秒)
     */
    public static long reloadValues() {
        long start = System.currentTimeMillis();
        PXEmc.registerValues();
        resendExactToAll();
        return System.currentTimeMillis() - start;
    }

    /**
     * 只把当前精确值表重推给所有在线玩家(不重算):PE 自己重载值表后走这条最短路径。
     *
     * @return 收到包的玩家数
     */
    public static int resendExactToAll() {
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null) {
            return 0;
        }
        List<?> players = server.getConfigurationManager().playerEntityList;
        if (players == null) {
            return 0;
        }
        int sent = 0;
        for (Object candidate : players) {
            if (candidate instanceof EntityPlayerMP) {
                PXExactEmcSync.sendTo((EntityPlayerMP) candidate);
                sent++;
            }
        }
        return sent;
    }
}
