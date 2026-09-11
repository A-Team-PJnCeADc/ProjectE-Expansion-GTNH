package com.projecte.expansion.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

/**
 * 能量凝聚器 MK3 的标记/状态方块实体。朝向存储在方块 metadata 中
 * (原版箱子约定:2=北, 3=南, 4=西, 5=东);冷凝器的物品栏/逻辑将在
 * 后续里程碑中实现。
 */
public class TileEntityCondenserMK3PX extends TileEntity {

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
    }
}
