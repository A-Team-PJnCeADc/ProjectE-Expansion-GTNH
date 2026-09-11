package com.projecte.expansion.integration;

import java.math.BigInteger;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import com.projecte.expansion.api.PXEmcValues;
import com.projecte.expansion.block.BlockTransmutationInterfacePX;
import com.projecte.expansion.registry.PXEmcAccess;
import com.projecte.expansion.registry.PXEmcTable;
import com.projecte.expansion.tile.TileEntityTransmutationInterfacePX;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;

/**
 * Waila 集成(1.7.10 里对应上游的 Jade):看向转化接口时显示所有者、生效 EMC、已学习数量与过滤模式。
 *
 * <p>
 * 通过 IMC 消息按类名注册({@code FMLInterModComms.sendMessage("Waila", "register", ...)}),
 * 没装 Waila 时这个类不会被加载,所以可以只做编译期依赖。
 *
 * <p>
 * 展示的数据由服务端 {@link #getNBTData} 打包,客户端只负责翻译,避免把整份知识列表同步到客户端。
 */
public class WailaProviderPX implements IWailaDataProvider {

    private static final String KEY_OWNER = "PXOwner";
    private static final String KEY_FILTER = "PXFilter";
    private static final String KEY_KNOWLEDGE = "PXKnowledge";
    private static final String KEY_EMC = "PXEmc";
    private static final String KEY_SHARED = "PXShared";

    /** Waila 会反射查找这个静态方法。 */
    public static void callBackRegister(IWailaRegistrar registrar) {
        WailaProviderPX provider = new WailaProviderPX();
        registrar.registerNBTProvider(provider, BlockTransmutationInterfacePX.class);
        registrar.registerBodyProvider(provider, BlockTransmutationInterfacePX.class);
    }

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack stack, List<String> tooltip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        return tooltip;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getWailaBody(ItemStack stack, List<String> tooltip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        NBTTagCompound tag = accessor.getNBTData();
        if (tag == null || !tag.hasKey(KEY_OWNER)) {
            return tooltip;
        }

        String owner = tag.getString(KEY_OWNER);
        boolean shared = tag.getBoolean(KEY_SHARED);
        tooltip.add(
            StatCollector.translateToLocalFormatted(
                shared ? "waila.projectexpansiongtnh.interface.owner_team"
                    : "waila.projectexpansiongtnh.interface.owner",
                owner));

        BigInteger emc = PXEmcTable.fromPe(tag.getDouble(KEY_EMC));
        tooltip.add(
            StatCollector.translateToLocalFormatted(
                "waila.projectexpansiongtnh.interface.emc",
                PXEmcValues.format(emc),
                PXEmcValues.formatExact(emc)));
        tooltip.add(
            StatCollector.translateToLocalFormatted(
                "waila.projectexpansiongtnh.interface.knowledge",
                tag.getInteger(KEY_KNOWLEDGE)));
        tooltip.add(
            StatCollector.translateToLocal(
                tag.getBoolean(KEY_FILTER) ? "waila.projectexpansiongtnh.interface.filter_on"
                    : "waila.projectexpansiongtnh.interface.filter_off"));
        return tooltip;
    }

    @Override
    public List<String> getWailaTail(ItemStack stack, List<String> tooltip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        return tooltip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x,
        int y, int z) {
        if (!(tile instanceof TileEntityTransmutationInterfacePX)) {
            return tag;
        }
        TileEntityTransmutationInterfacePX iface = (TileEntityTransmutationInterfacePX) tile;
        tag.setString(KEY_OWNER, iface.getOwnerName());
        tag.setBoolean(KEY_FILTER, iface.isNbtFilterEnabled());
        tag.setInteger(KEY_KNOWLEDGE, iface.getKnowledgeCount());

        net.minecraft.entity.player.EntityPlayer owner = iface.getOwnerPlayer();
        if (owner != null) {
            tag.setDouble(KEY_EMC, PXEmcAccess.getEmc(owner));
            tag.setBoolean(KEY_SHARED, PXEmcAccess.isShared(owner));
        } else {
            tag.setDouble(KEY_EMC, 0D);
            tag.setBoolean(KEY_SHARED, false);
        }
        return tag;
    }
}
