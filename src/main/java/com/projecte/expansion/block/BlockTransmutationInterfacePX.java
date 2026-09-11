package com.projecte.expansion.block;

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
import com.projecte.expansion.tile.TileEntityTransmutationInterfacePX;

import moze_intel.projecte.gameObjs.ObjHandler;

/**
 * 转化接口(Transmutation Interface):把归属者的转化桌接到 AE / 管道的物品网络上。
 *
 * <p>
 * 元数据同时是过滤模式(也决定贴图):
 * <ul>
 * <li>{@link #META_NBT_FILTER}(0,红,默认)= 启用 NBT 过滤:带 NBT 或已消耗耐久的物品不接收;</li>
 * <li>{@link #META_NO_FILTER}(1,蓝)= 忽略一切 NBT,按物品基础价转化。</li>
 * </ul>
 * 所有者用贤者之石潜行右击切换;普通右击显示所有者。方块实体见
 * {@link TileEntityTransmutationInterfacePX}。
 */
public class BlockTransmutationInterfacePX extends BlockContainer {

    /** 红:启用 NBT 过滤(默认)。 */
    public static final int META_NBT_FILTER = 0;
    /** 蓝:关闭 NBT 过滤。 */
    public static final int META_NO_FILTER = 1;

    private IIcon[] icons;

    public BlockTransmutationInterfacePX() {
        super(Material.iron);
        setBlockName("transmutation_interface");
        setHardness(5.0F);
        setResistance(10.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityTransmutationInterfacePX();
    }

    @Override
    public void registerBlockIcons(IIconRegister register) {
        icons = new IIcon[] { register.registerIcon(MyMod.MODID + ":transmutation_interface"),
            register.registerIcon(MyMod.MODID + ":transmutation_interface_nofilter") };
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return icons[meta & 1];
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        world.setBlockMetadataWithNotify(x, y, z, META_NBT_FILTER, 2);
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityTransmutationInterfacePX && placer instanceof EntityPlayer) {
            ((TileEntityTransmutationInterfacePX) tile).setOwner((EntityPlayer) placer);
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof TileEntityTransmutationInterfacePX)) {
            return false;
        }
        if (world.isRemote) {
            return true;
        }

        TileEntityTransmutationInterfacePX iface = (TileEntityTransmutationInterfacePX) tile;
        ItemStack held = player.getCurrentEquippedItem();
        boolean holdingStone = held != null && held.getItem() == ObjHandler.philosStone;

        if (player.isSneaking() && holdingStone) {
            if (!iface.isOwner(player)) {
                player.addChatMessage(
                    new ChatComponentTranslation(
                        "chat.projectexpansiongtnh.interface.not_owner",
                        iface.getOwnerName()));
                return true;
            }
            int meta = (world.getBlockMetadata(x, y, z) & 1) == META_NBT_FILTER ? META_NO_FILTER : META_NBT_FILTER;
            world.setBlockMetadataWithNotify(x, y, z, meta, 2);
            player.addChatMessage(
                new ChatComponentTranslation(
                    meta == META_NBT_FILTER ? "chat.projectexpansiongtnh.interface.filter_on"
                        : "chat.projectexpansiongtnh.interface.filter_off"));
            return true;
        }

        player.addChatMessage(
            new ChatComponentTranslation("chat.projectexpansiongtnh.interface.owner", iface.getOwnerName()));
        return true;
    }

}
