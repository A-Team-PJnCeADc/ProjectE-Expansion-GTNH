package com.projecte.expansion.api;

import java.math.BigInteger;

import net.minecraft.item.ItemStack;

/**
 * 供其他 mod（AutoEMC-GTNH、ProjectE-Team 等）使用的可选集成接口：实现本接口并以
 * {@link PXEmcValues#addProvider(IPXEmcProvider)} 注册，即可提供精确的 BigInteger EMC
 * 值，无需预先计算每一个物品。
 *
 * <p>
 * 提供器的查询顺序位于本 mod 自身数值之后、ProjectE 受 int 上限限制的表之前，
 * 因此提供器给出的值就是转化桌、冷凝器和（自 M3 起）本 mod 机器所使用的值——
 * 包括超过 Integer.MAX_VALUE 的数值，
 * 而 ProjectE 自己的表无法容纳这些值。
 *
 * <p>
 * 实现必须快速、线程安全，且对不处理的物品堆必须返回 {@code null}（而不是 0）；
 * 每次查询价格时都会调用它们，逻辑客户端与服务端两侧皆是如此。
 */
public interface IPXEmcProvider {

    /** @return 精确值；若该提供器无法为此物品堆定价则返回 null。 */
    BigInteger getEmcValue(ItemStack stack);
}
