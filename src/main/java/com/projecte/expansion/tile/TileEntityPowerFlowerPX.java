package com.projecte.expansion.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

/**
 * 力量花的标记/状态方块实体（上游用复合方块模型渲染它们，这在 1.7.10 中需要一个 TESR）。
 * 产出/逻辑将随机器里程碑一起加入。
 */
public class TileEntityPowerFlowerPX extends TileEntity {

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
    }
}
