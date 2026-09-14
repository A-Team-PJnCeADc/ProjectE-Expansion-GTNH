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
import com.projecte.expansion.tile.TileEntityCollectorPX;
import com.projecte.expansion.util.EMCFormatPX;
import com.projecte.expansion.util.Matter;

/**
 * 能量收集器(Collector):以层级速率持续产生 EMC,并当次就交给四周能接收 EMC 的方块
 * (见 {@link TileEntityCollectorPX})。
 *
 * <p>
 * 方块本体没有界面、没有物品栏、也不保存 EMC:右击显示所有者与每秒产出(Waila 里同样会显示)。
 */
public class BlockCollectorPX extends BlockContainer {

    public final Matter matter;
    private final String iconPath;
    private IIcon icon;

    public BlockCollectorPX(Matter matter) {
        super(Material.iron);
        this.matter = matter;
        String tier = matter.name()
            .toLowerCase(Locale.US);
        this.iconPath = "collector/" + tier;
        setBlockName(tier + "_collector");
        setHardness(5.0F);
        setResistance(10.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityCollectorPX(matter);
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
        if (tile instanceof TileEntityCollectorPX) {
            ((TileEntityCollectorPX) tile).setOwner((EntityPlayer) placer);
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof TileEntityCollectorPX)) {
            return false;
        }
        if (world.isRemote) {
            return true;
        }
        // 收集器没有存量也没有界面:右击显示所有者与每秒产出
        TileEntityCollectorPX collector = (TileEntityCollectorPX) tile;
        player.addChatMessage(
            new ChatComponentTranslation("chat.projectexpansiongtnh.interface.owner", collector.getOwnerName()));
        player.addChatMessage(
            new ChatComponentTranslation(
                "block.projectexpansion.collector.emc",
                EMCFormatPX.format(collector.getPerSecondOutput())));
        return true;
    }
}
