package com.projecte.expansion.block;

import net.minecraft.block.material.Material;

import com.projecte.expansion.util.Matter;

/** 真正收集器的占位固体方块(TE/GUI 会在后续里程碑加入)。 */
public class BlockCollectorPX extends BlockPXBase {

    public final Matter matter;

    public BlockCollectorPX(Matter matter) {
        super(
            matter.name()
                .toLowerCase(java.util.Locale.US) + "_collector",
            Material.iron,
            5.0F,
            10.0F,
            "collector/" + matter.name()
                .toLowerCase(java.util.Locale.US));
        this.matter = matter;
    }
}
