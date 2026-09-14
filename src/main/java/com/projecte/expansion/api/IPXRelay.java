package com.projecte.expansion.api;

/**
 * 继电器对外的最小约定:相邻的收集器每次产出时都会调用 {@link #addRelayBonus(int)}。
 *
 * <p>
 * 加成的数量由**继电器自己的层级**决定(见 {@link com.projecte.expansion.util.Matter#relayBonus}),
 * 与给它的收集器是哪一级无关 —— 和 ProjectE 里收集器给中继器发加成是同一套做法。
 */
public interface IPXRelay {

    /**
     * 由相邻收集器调用,把这一段的继电器加成计入自身存量。
     *
     * @param ticks 这一段覆盖的 tick 数(收集器的 tickDelay):继电器按 {@code bonus × ticks / 20} 折算,
     *              所以改 tickDelay 不会把加成放大或缩小。
     */
    void addRelayBonus(int ticks);
}
