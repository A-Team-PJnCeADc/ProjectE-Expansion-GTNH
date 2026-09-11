package com.projecte.expansion.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

/**
 * 高级炼金箱的标记/状态方块实体。朝向存储在方块元数据中
 * （原版箱子约定：2=北, 3=南, 4=西, 5=东）；
 * 物品栏将在后续里程碑中加入。
 */
public class TileEntityAdvancedChestPX extends TileEntity {

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
    }
}
