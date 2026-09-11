package com.projecte.expansion.block;

import net.minecraft.block.material.Material;

import com.projecte.expansion.util.Matter;

/** 每个等级两个图标：普通版和 "nofilter" 变体；过滤状态稍后由元数据表示。 */
public class BlockEMCLinkPX extends BlockPXBase {

    public final Matter matter;

    public BlockEMCLinkPX(Matter matter) {
        super(
            matter.name()
                .toLowerCase(java.util.Locale.US) + "_emc_link",
            Material.iron,
            5.0F,
            10.0F,
            "emc_link/" + matter.name()
                .toLowerCase(java.util.Locale.US),
            "emc_link/" + matter.name()
                .toLowerCase(java.util.Locale.US) + "_nofilter");
        this.matter = matter;
    }
}
