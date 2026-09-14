package com.projecte.expansion.api;

import java.math.BigInteger;

import net.minecraftforge.common.util.ForgeDirection;

/**
 * 精确版 EMC 接收方:本 mod 机器之间用 BigInteger 交接,避免 PE 1.7.10 的 double 接口
 * 在 2^53 以上丢个位(顶级继电器的每秒传输量已经是 9.2e18 量级)。
 *
 * <p>
 * 收集器/继电器在推 EMC 时优先走这里,对方没实现时才回退到 PE 的
 * {@link moze_intel.projecte.api.tile.IEmcAcceptor#acceptEMC(ForgeDirection, double)}。
 */
public interface IPXEmcAcceptorBig {

    /**
     * 接收至多 {@code toAccept} 的 EMC。
     *
     * @return 实际接收量(0 表示拒收)
     */
    BigInteger acceptEmcBig(ForgeDirection side, BigInteger toAccept);
}
