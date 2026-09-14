package com.projecte.expansion.block;

import java.math.BigInteger;
import java.util.Locale;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import com.projecte.expansion.MyMod;
import com.projecte.expansion.tile.TileEntityEMCLinkPX;
import com.projecte.expansion.util.EMCFormatPX;
import com.projecte.expansion.util.Matter;

import moze_intel.projecte.gameObjs.ObjHandler;

/**
 * EMC 接口(EMC Link):把 EMC 注入所有者(团队共享时是团队池),并让所有者用 EMC 换物品/流体。
 * 机器本体见 {@link TileEntityEMCLinkPX}。
 *
 * <p>
 * 操作(所有者):
 * <ul>
 * <li>手持物品右击 = 绑定为该接口的导出物品(流体则手持对应的桶),这一次只设置、不给物品;</li>
 * <li>绑定后的右击 = 只看信息(所有者、导出物品/流体、每秒上限)—— **导出只能靠管道**:</li>
 * <li>管道从导出槽(槽 0)抽物品、从流体接口抽液体,按精确价扣所有者 EMC,每秒上限见表,不累积;</li>
 * <li>管道往导入槽(槽 1 起)塞物品 = 按精确价折算成所有者 EMC,每秒上限见表;</li>
 * <li>空手潜行右击 = 清除绑定;</li>
 * <li>贤者之石潜行右击 = 切换 NBT 过滤(默认过滤带 NBT/耐久的物品,关闭后贴图变黄);</li>
 * <li>非所有者右击只显示所有者。</li>
 * </ul>
 */
public class BlockEMCLinkPX extends BlockContainer {

    /** 红:启用 NBT 过滤(默认)。 */
    public static final int META_NBT_FILTER = 0;
    /** 黄:关闭 NBT 过滤。 */
    public static final int META_NO_FILTER = 1;

    public final Matter matter;
    private IIcon[] icons;

    public BlockEMCLinkPX(Matter matter) {
        super(Material.iron);
        this.matter = matter;
        String tier = matter.name()
            .toLowerCase(Locale.US);
        setBlockName(tier + "_emc_link");
        setHardness(5.0F);
        setResistance(10.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityEMCLinkPX(matter);
    }

    @Override
    public void registerBlockIcons(IIconRegister register) {
        String tier = matter.name()
            .toLowerCase(Locale.US);
        icons = new IIcon[] { register.registerIcon(MyMod.MODID + ":emc_link/" + tier),
            register.registerIcon(MyMod.MODID + ":emc_link/" + tier + "_nofilter") };
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return icons[meta & 1];
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        world.setBlockMetadataWithNotify(x, y, z, META_NBT_FILTER, 2);
        if (!(placer instanceof EntityPlayer)) {
            return;
        }
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityEMCLinkPX) {
            ((TileEntityEMCLinkPX) tile).setOwner((EntityPlayer) placer);
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof TileEntityEMCLinkPX)) {
            return false;
        }
        if (world.isRemote) {
            return true;
        }

        TileEntityEMCLinkPX link = (TileEntityEMCLinkPX) tile;
        ItemStack held = player.getCurrentEquippedItem();
        boolean holdingStone = held != null && held.getItem() == ObjHandler.philosStone;
        boolean sneaking = player.isSneaking();

        if (!link.isOwner(player)) {
            player.addChatMessage(
                new ChatComponentTranslation("chat.projectexpansiongtnh.interface.owner", link.getOwnerName()));
            if (holdingStone && sneaking) {
                player.addChatMessage(
                    new ChatComponentTranslation("chat.projectexpansiongtnh.interface.not_owner", link.getOwnerName()));
            }
            return true;
        }

        // 贤者之石潜行右击:切换 NBT 过滤(贴图跟着变黄/变红)
        if (holdingStone && sneaking) {
            int meta = (world.getBlockMetadata(x, y, z) & 1) == META_NBT_FILTER ? META_NO_FILTER : META_NBT_FILTER;
            world.setBlockMetadataWithNotify(x, y, z, meta, 2);
            player.addChatMessage(
                new ChatComponentTranslation(
                    meta == META_NBT_FILTER ? "chat.projectexpansiongtnh.emc_link.filter_on"
                        : "chat.projectexpansiongtnh.emc_link.filter_off"));
            return true;
        }

        // 空手潜行右击:清除绑定
        if (sneaking && held == null) {
            if (!link.hasBoundItem()) {
                player.addChatMessage(new ChatComponentTranslation("block.projectexpansion.emc_link.not_set"));
                return true;
            }
            link.clearBoundItem();
            player.addChatMessage(new ChatComponentTranslation("block.projectexpansion.emc_link.cleared"));
            return true;
        }

        // 尚未绑定:手持物品即绑定,并**在这次右击里立刻**按所有者 EMC 给出物品
        if (!link.hasBoundItem()) {
            if (held == null) {
                player.addChatMessage(new ChatComponentTranslation("block.projectexpansion.emc_link.not_set"));
                return true;
            }
            if (!link.accepts(held)) {
                player.addChatMessage(
                    new ChatComponentTranslation(
                        "block.projectexpansion.emc_link.no_emc_value",
                        held.getDisplayName()));
                return true;
            }
            link.bindItem(held);
            player.addChatMessage(
                new ChatComponentTranslation(
                    "block.projectexpansion.emc_link.set",
                    link.getBoundItem()
                        .getDisplayName()));
            // 第一次右击只设置物品,不给物品(上游就是这样)
            return true;
        }

        // 已绑定后的右击 = 只看信息:物品/流体只能靠管道输出(右击不再直接给东西)
        showInfo(player, link);
        return true;
    }

    /** 右击查看:所有者、导出物品/流体、每秒速率与上限。 */
    private void showInfo(EntityPlayer player, TileEntityEMCLinkPX link) {
        player.addChatMessage(
            new ChatComponentTranslation("chat.projectexpansiongtnh.interface.owner", link.getOwnerName()));
        ItemStack bound = link.getBoundItem();
        if (bound == null) {
            player.addChatMessage(new ChatComponentTranslation("block.projectexpansion.emc_link.not_set"));
            return;
        }
        player.addChatMessage(
            new ChatComponentTranslation("block.projectexpansion.emc_link.set", bound.getDisplayName()));
        if (link.hasBoundFluid()) {
            player.addChatMessage(
                new ChatComponentTranslation(
                    "waila.projectexpansiongtnh.emc_link.fluid",
                    link.getBoundFluid()
                        .getLocalizedName()));
        }
        player.addChatMessage(
            new ChatComponentTranslation(
                "block.projectexpansion.emc_link.limit_items",
                EMCFormatPX.format(BigInteger.valueOf(link.getPerSecondItemLimit()))));
        if (link.hasBoundFluid()) {
            player.addChatMessage(
                new ChatComponentTranslation(
                    "block.projectexpansion.emc_link.limit_fluids",
                    EMCFormatPX.format(BigInteger.valueOf(link.getPerSecondFluidLimit()))));
        }
        player.addChatMessage(new ChatComponentTranslation("block.projectexpansion.emc_link.tooltip"));
    }
}
