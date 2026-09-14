package com.projecte.expansion.block;

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
import com.projecte.expansion.tile.TileEntityRelayPX;
import com.projecte.expansion.util.EMCFormatPX;
import com.projecte.expansion.util.Matter;

/**
 * 反物质继电器(Relay):传输 EMC。存量、每秒传输上限与继电器加成见 {@link TileEntityRelayPX}。
 *
 * <p>
 * 紧贴一个 EMC 接口就能把 EMC 注入玩家/团队 EMC 系统;继电器自己不做注入。
 * 右击显示所有者、继电器加成与最大传输速率(Waila 里同样会显示)。
 */
public class BlockRelayPX extends BlockContainer {

    public final Matter matter;
    private final String iconPath;
    private IIcon icon;

    public BlockRelayPX(Matter matter) {
        super(Material.iron);
        this.matter = matter;
        String tier = matter.name()
            .toLowerCase(Locale.US);
        this.iconPath = "relay/" + tier;
        setBlockName(tier + "_relay");
        setHardness(5.0F);
        setResistance(10.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityRelayPX(matter);
    }

    @Override
    public void registerBlockIcons(IIconRegister register) {
        icon = register.registerIcon(MyMod.MODID + ":" + iconPath);
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return icon;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        if (!(placer instanceof EntityPlayer)) {
            return;
        }
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityRelayPX) {
            ((TileEntityRelayPX) tile).setOwner((EntityPlayer) placer);
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof TileEntityRelayPX)) {
            return false;
        }
        if (world.isRemote) {
            return true;
        }
        TileEntityRelayPX relay = (TileEntityRelayPX) tile;
        player.addChatMessage(
            new ChatComponentTranslation("chat.projectexpansiongtnh.interface.owner", relay.getOwnerName()));
        player.addChatMessage(
            new ChatComponentTranslation(
                "block.projectexpansion.relay.bonus",
                EMCFormatPX.format(relay.getPerSecondBonus())));
        player.addChatMessage(
            new ChatComponentTranslation(
                "block.projectexpansion.relay.transfer",
                EMCFormatPX.format(relay.getPerSecondTransfer())));
        return true;
    }
}
