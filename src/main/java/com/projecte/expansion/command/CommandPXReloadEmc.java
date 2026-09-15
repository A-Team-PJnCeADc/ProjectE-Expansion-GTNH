package com.projecte.expansion.command;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentTranslation;

import com.projecte.expansion.Config;
import com.projecte.expansion.MyMod;
import com.projecte.expansion.registry.PXEmcRefresher;

import moze_intel.projecte.config.CustomEMCParser;
import moze_intel.projecte.emc.EMCMapper;
import moze_intel.projecte.handlers.TileEntityHandler;
import moze_intel.projecte.network.PacketHandler;

/**
 * {@code /projectexpansion reloademc}:重载 EMC 数值(上游 Project Expansion 同名命令的 1.7.10 版,
 * 由 {@link Config#enableReloadEMCCommand} 开关)。
 *
 * <p>
 * 做的三步:
 * <ol>
 * <li>照 {@code /projecte reloadEMC} 的做法让 ProjectE 重算一遍值表(读自定义值文件 -> 重新 map ->
 * 检查凝聚器);</li>
 * <li>重算本模组的精确值(BigInteger)并把它重新推给所有在线玩家 —— 这是 PE 那条命令**不做**的部分,
 * 也是"重载完客户端还是旧值"的根因;</li>
 * <li>把 PE 的 int 值表也重发一遍(与 PE 的命令一致)。</li>
 * </ol>
 */
public class CommandPXReloadEmc extends CommandBase {

    @Override
    public String getCommandName() {
        return "projectexpansion";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/projectexpansion reloademc";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 4;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return Config.enableReloadEMCCommand && super.canCommandSenderUseCommand(sender);
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1 || !"reloademc".equalsIgnoreCase(args[0])) {
            sender.addChatMessage(
                new ChatComponentTranslation("command.projectexpansion.reload.notice", getCommandUsage(sender)));
            return;
        }

        sender.addChatMessage(new ChatComponentTranslation("command.projectexpansion.reload_emc.warning"));
        sender.addChatMessage(new ChatComponentTranslation("command.projectexpansion.reloading_emc"));

        // 1) ProjectE 自己重算(与 /projecte reloadEMC 完全相同的步骤)
        EMCMapper.clearMaps();
        CustomEMCParser.readUserData();
        EMCMapper.map();
        TileEntityHandler.checkAllCondensers();

        // 2) 本模组的精确值重算 + 推给所有在线玩家
        long elapsed = PXEmcRefresher.reloadValues();

        // 3) ProjectE 的 int 值表也重发给所有人
        PacketHandler.sendFragmentedEmcPacketToAll();

        int count = EMCMapper.emc == null ? 0 : EMCMapper.emc.size();
        sender.addChatMessage(
            new ChatComponentTranslation(
                "command.projectexpansion.reload_emc.success",
                String.valueOf(count),
                elapsed + "ms"));
        MyMod.LOG.info("Reloaded EMC values by command: {} entries in {}ms.", count, elapsed);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, "reloademc") : null;
    }
}
