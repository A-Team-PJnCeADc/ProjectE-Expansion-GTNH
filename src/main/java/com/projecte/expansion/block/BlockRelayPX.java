package com.projecte.expansion.block;

import net.minecraft.block.material.Material;

import com.projecte.expansion.util.Matter;

public class BlockRelayPX extends BlockPXBase {

    public final Matter matter;

    public BlockRelayPX(Matter matter) {
        super(
            matter.name()
                .toLowerCase(java.util.Locale.US) + "_relay",
            Material.iron,
            5.0F,
            10.0F,
            "relay/" + matter.name()
                .toLowerCase(java.util.Locale.US));
        this.matter = matter;
    }
}
