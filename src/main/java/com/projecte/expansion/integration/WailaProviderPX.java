package com.projecte.expansion.integration;

import java.math.BigInteger;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import com.projecte.expansion.api.PXEmcValues;
import com.projecte.expansion.block.BlockCollectorPX;
import com.projecte.expansion.block.BlockEMCLinkPX;
import com.projecte.expansion.block.BlockPowerFlowerPX;
import com.projecte.expansion.block.BlockRelayPX;
import com.projecte.expansion.block.BlockTransmutationInterfacePX;
import com.projecte.expansion.registry.PXEmcAccess;
import com.projecte.expansion.registry.PXEmcTable;
import com.projecte.expansion.tile.TileEntityCollectorPX;
import com.projecte.expansion.tile.TileEntityEMCLinkPX;
import com.projecte.expansion.tile.TileEntityPowerFlowerPX;
import com.projecte.expansion.tile.TileEntityRelayPX;
import com.projecte.expansion.tile.TileEntityTransmutationInterfacePX;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;

/**
 * Waila 集成(1.7.10 里对应上游的 Jade):看向本 mod 的机器时显示**所有者**和**速率**。
 *
 * <ul>
 * <li>能量收集器:所有者、每秒产出</li>
 * <li>继电器:所有者、继电器加成、每秒传输上限、当前存量</li>
 * <li>EMC 接口:所有者、绑定物品/流体、NBT 过滤状态、每秒 EMC/物品/流体上限</li>
 * <li>力量花:所有者、每秒产出</li>
 * <li>转化接口:所有者、生效 EMC、已学习数量、过滤模式</li>
 * </ul>
 *
 * <p>
 * 通过 IMC 消息按类名注册({@code FMLInterModComms.sendMessage("Waila", "register", …)}),
 * 没装 Waila 时这个类不会被加载,所以可以只做编译期依赖。
 *
 * <p>
 * 展示的数据由服务端 {@link #getNBTData} 打包成字符串/数值,客户端只负责翻译,避免把 BigInteger
 * 或整份知识列表同步到客户端。
 */
public class WailaProviderPX implements IWailaDataProvider {

    /** 机器种类:决定正文里显示哪些行。 */
    private static final String KEY_KIND = "PXKind";
    private static final String KEY_OWNER = "PXOwner";
    private static final String KEY_HAS_OWNER = "PXHasOwner";
    private static final String KEY_SHARED = "PXShared";
    private static final String KEY_FILTER = "PXFilter";
    private static final String KEY_KNOWLEDGE = "PXKnowledge";
    private static final String KEY_EMC = "PXEmc";
    private static final String KEY_RATE = "PXRate";
    private static final String KEY_BONUS = "PXBonus";
    private static final String KEY_STORED = "PXStored";
    private static final String KEY_BOUND = "PXBound";
    private static final String KEY_BOUND_FLUID = "PXBoundFluid";
    private static final String KEY_EMC_LIMIT = "PXEmcLimit";
    private static final String KEY_ITEMS_LIMIT = "PXItemLimit";
    private static final String KEY_FLUID_LIMIT = "PXFluidLimit";

    private static final String KIND_COLLECTOR = "collector";
    private static final String KIND_RELAY = "relay";
    private static final String KIND_LINK = "link";
    private static final String KIND_FLOWER = "flower";
    private static final String KIND_INTERFACE = "interface";

    /** Waila 会反射查找这个静态方法。 */
    public static void callBackRegister(IWailaRegistrar registrar) {
        WailaProviderPX provider = new WailaProviderPX();
        for (Class<?> block : new Class<?>[] { BlockCollectorPX.class, BlockRelayPX.class, BlockEMCLinkPX.class,
            BlockPowerFlowerPX.class, BlockTransmutationInterfacePX.class }) {
            registrar.registerNBTProvider(provider, block);
            registrar.registerBodyProvider(provider, block);
        }
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
        if (tag == null) {
            return tooltip;
        }
        String kind = tag.getString(KEY_KIND);
        if (kind.isEmpty()) {
            return tooltip;
        }

        // 所有者:没有所有者的机器(比如旧版本摆下的方块)显示"无"
        if (tag.getBoolean(KEY_HAS_OWNER)) {
            tooltip.add(
                StatCollector.translateToLocalFormatted(
                    tag.getBoolean(KEY_SHARED) ? "waila.projectexpansiongtnh.interface.owner_team"
                        : "waila.projectexpansiongtnh.interface.owner",
                    tag.getString(KEY_OWNER)));
        } else {
            tooltip.add(StatCollector.translateToLocal("waila.projectexpansiongtnh.owner_none"));
        }

        if (KIND_COLLECTOR.equals(kind)) {
            tooltip.add(line("block.projectexpansion.collector.emc", tag.getString(KEY_RATE)));
        } else if (KIND_RELAY.equals(kind)) {
            tooltip.add(line("block.projectexpansion.relay.bonus", tag.getString(KEY_BONUS)));
            tooltip.add(line("block.projectexpansion.relay.transfer", tag.getString(KEY_RATE)));
            tooltip.add(line("waila.projectexpansiongtnh.stored", tag.getString(KEY_STORED)));
        } else if (KIND_FLOWER.equals(kind)) {
            tooltip.add(line("block.projectexpansion.power_flower.emc", tag.getString(KEY_RATE)));
        } else if (KIND_LINK.equals(kind)) {
            linkBody(tag, tooltip);
        } else {
            interfaceBody(tag, tooltip);
        }
        return tooltip;
    }

    @Override
    public List<String> getWailaTail(ItemStack stack, List<String> tooltip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        return tooltip;
    }

    // 打包(服务端)

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x,
        int y, int z) {
        if (tile instanceof TileEntityCollectorPX) {
            TileEntityCollectorPX collector = (TileEntityCollectorPX) tile;
            tag.setString(KEY_KIND, KIND_COLLECTOR);
            tag.setString(KEY_RATE, PXEmcValues.format(collector.getPerSecondOutput()));
            ownerData(tag, collector.hasOwner(), collector.getOwnerName(), null);
        } else if (tile instanceof TileEntityRelayPX) {
            TileEntityRelayPX relay = (TileEntityRelayPX) tile;
            tag.setString(KEY_KIND, KIND_RELAY);
            tag.setString(KEY_RATE, PXEmcValues.format(relay.getPerSecondTransfer()));
            tag.setString(KEY_BONUS, PXEmcValues.format(relay.getPerSecondBonus()));
            tag.setString(KEY_STORED, PXEmcValues.format(relay.getStoredEmcBig()));
            ownerData(tag, relay.hasOwner(), relay.getOwnerName(), null);
        } else if (tile instanceof TileEntityPowerFlowerPX) {
            TileEntityPowerFlowerPX flower = (TileEntityPowerFlowerPX) tile;
            tag.setString(KEY_KIND, KIND_FLOWER);
            tag.setString(KEY_RATE, PXEmcValues.format(flower.getPerSecondOutput()));
            ownerData(tag, flower.hasOwner(), flower.getOwnerName(), null);
        } else if (tile instanceof TileEntityEMCLinkPX) {
            linkNbtData((TileEntityEMCLinkPX) tile, tag);
        } else if (tile instanceof TileEntityTransmutationInterfacePX) {
            interfaceNbtData((TileEntityTransmutationInterfacePX) tile, tag);
        }
        return tag;
    }

    private void ownerData(NBTTagCompound tag, boolean hasOwner, String ownerName, EntityPlayer owner) {
        tag.setBoolean(KEY_HAS_OWNER, hasOwner);
        tag.setString(KEY_OWNER, ownerName == null ? "" : ownerName);
        if (owner != null) {
            tag.setDouble(KEY_EMC, PXEmcAccess.getEmc(owner));
            tag.setBoolean(KEY_SHARED, PXEmcAccess.isShared(owner));
        } else {
            tag.setBoolean(KEY_SHARED, false);
        }
    }

    /** EMC 接口:所有者、绑定物品/流体、NBT 过滤状态、每秒各项上限。 */
    private NBTTagCompound linkNbtData(TileEntityEMCLinkPX link, NBTTagCompound tag) {
        tag.setString(KEY_KIND, KIND_LINK);
        tag.setBoolean(KEY_FILTER, link.isNbtFilterEnabled());

        ItemStack bound = link.getBoundItem();
        tag.setString(KEY_BOUND, bound == null ? "" : bound.getDisplayName());
        FluidStack fluid = link.getBoundFluid();
        tag.setString(KEY_BOUND_FLUID, fluid == null ? "" : fluid.getLocalizedName());

        BigInteger emcLimit = link.getPerSecondEmcLimit();
        tag.setString(KEY_EMC_LIMIT, emcLimit == null ? "" : PXEmcValues.format(emcLimit));
        tag.setString(
            KEY_ITEMS_LIMIT,
            limitText(BigInteger.valueOf(link.getPerSecondItemLimit()), link.isItemLimitUnlimited()));
        tag.setString(
            KEY_FLUID_LIMIT,
            limitText(BigInteger.valueOf(link.getPerSecondFluidLimit()), link.isFluidLimitUnlimited()));

        EntityPlayer owner = link.getOwnerPlayer();
        ownerData(tag, true, link.getOwnerName(), owner);
        if (owner == null) {
            tag.setDouble(KEY_EMC, 0D);
        }
        return tag;
    }

    /** 转化接口:所有者、生效 EMC、已学习数量、过滤模式。 */
    private NBTTagCompound interfaceNbtData(TileEntityTransmutationInterfacePX iface, NBTTagCompound tag) {
        tag.setString(KEY_KIND, KIND_INTERFACE);
        tag.setBoolean(KEY_FILTER, iface.isNbtFilterEnabled());
        tag.setInteger(KEY_KNOWLEDGE, iface.getKnowledgeCount());

        EntityPlayer owner = iface.getOwnerPlayer();
        ownerData(tag, true, iface.getOwnerName(), owner);
        if (owner == null) {
            tag.setDouble(KEY_EMC, 0D);
        }
        return tag;
    }

    private static String limitText(BigInteger limit, boolean unlimited) {
        return unlimited ? "" : PXEmcValues.format(limit);
    }

    // 渲染(客户端)

    /** EMC 接口的正文。 */
    private List<String> linkBody(NBTTagCompound tag, List<String> tooltip) {
        String bound = tag.getString(KEY_BOUND);
        String fluid = tag.getString(KEY_BOUND_FLUID);
        tooltip.add(
            StatCollector.translateToLocal(
                bound.isEmpty() ? "waila.projectexpansiongtnh.emc_link.none"
                    : "waila.projectexpansiongtnh.emc_link.bound_plain"));
        if (!bound.isEmpty()) {
            tooltip.add(line("waila.projectexpansiongtnh.emc_link.bound", bound));
        }
        if (!fluid.isEmpty()) {
            tooltip.add(line("waila.projectexpansiongtnh.emc_link.fluid", fluid));
        }
        tooltip.add(
            StatCollector.translateToLocal(
                tag.getBoolean(KEY_FILTER) ? "waila.projectexpansiongtnh.emc_link.filter_on"
                    : "waila.projectexpansiongtnh.emc_link.filter_off"));
        tooltip.add(line("block.projectexpansion.emc_link.limit_emc", orUnlimited(tag.getString(KEY_EMC_LIMIT))));
        tooltip.add(line("block.projectexpansion.emc_link.limit_items", orUnlimited(tag.getString(KEY_ITEMS_LIMIT))));
        tooltip.add(line("block.projectexpansion.emc_link.limit_fluids", orUnlimited(tag.getString(KEY_FLUID_LIMIT))));
        return tooltip;
    }

    /** 转化接口的正文。 */
    private List<String> interfaceBody(NBTTagCompound tag, List<String> tooltip) {
        BigInteger emc = PXEmcTable.fromPe(tag.getDouble(KEY_EMC));
        tooltip.add(
            line("waila.projectexpansiongtnh.interface.emc", PXEmcValues.format(emc), PXEmcValues.formatExact(emc)));
        tooltip
            .add(line("waila.projectexpansiongtnh.interface.knowledge", String.valueOf(tag.getInteger(KEY_KNOWLEDGE))));
        tooltip.add(
            StatCollector.translateToLocal(
                tag.getBoolean(KEY_FILTER) ? "waila.projectexpansiongtnh.interface.filter_on"
                    : "waila.projectexpansiongtnh.interface.filter_off"));
        return tooltip;
    }

    private static String orUnlimited(String value) {
        return value == null || value.isEmpty() ? StatCollector.translateToLocal("waila.projectexpansiongtnh.unlimited")
            : value;
    }

    private static String line(String key, Object... args) {
        return StatCollector.translateToLocalFormatted(key, args);
    }
}
