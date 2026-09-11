package com.projecte.expansion.api;

import java.math.BigInteger;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.projecte.expansion.registry.PXEmcTable;

/**
 * 所有者 EMC / 知识访问接口(对外扩展点)。
 *
 * <p>
 * 本 mod 的转化接口(Transmutation Interface)不直接读写玩家的 EMC 与知识,而是全部经过这里;
 * 默认实现走 ProjectE 自己的 {@code Transmutation}(装了 ProjectE-Team 时会自动团队化 ——
 * 团队共享开启时 ProjectE 的读写就被代理到团队池),仓库里还带一个直接面向
 * {@code com.projecte.team.TeamManager} 的反射实现。
 *
 * <p>
 * 其它 mod(如 ProjectE-Team)可以注册自己的实现,把团队/自定义数据的语义讲清楚:
 * 
 * <pre>
 * PXEmcAccess.register(new MyEmcAccess());
 * </pre>
 */
public interface IPXEmcAccess {

    /** @return 所有者当前可用的 EMC(团队共享时为团队池)。 */
    double getEmc(EntityPlayer owner);

    /**
     * 增加(amount &gt; 0)或扣除(amount &lt; 0)所有者的 EMC。
     *
     * <p>
     * 必须是**增量**语义:团队共享模式下 ProjectE 的绝对写入会被忽略,只有增量才能落到团队池。
     */
    void addEmc(EntityPlayer owner, double amount);

    /** @return 所有者已学习的物品(团队共享知识时为团队列表),调用方不得修改返回列表。 */
    List<ItemStack> getKnowledge(EntityPlayer owner);

    /** @return 该所有者当前是否在使用团队(共享)数据。 */
    boolean isShared(EntityPlayer owner);

    /**
     * @return 所有者当前可用的 EMC 的**精确**值。默认实现把 double 值转成 BigInteger;
     *         能提供 BigInteger 存储的实现(如装了 PE-E-GTNH 的 ProjectE-Team 团队池)应覆写它。
     */
    default BigInteger getEmcBig(EntityPlayer owner) {
        return PXEmcTable.fromPe(getEmc(owner));
    }

    /**
     * 按精确值增加/扣除(delta 为负表示扣除)。
     *
     * @return true 表示已按精确值处理;false 表示实现不支持,调用方应回退到 {@link #addEmc}
     */
    default boolean addEmcBig(EntityPlayer owner, BigInteger delta) {
        return false;
    }
}
