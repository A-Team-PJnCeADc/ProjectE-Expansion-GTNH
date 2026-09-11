package com.projecte.expansion.integration;

import java.lang.reflect.Method;
import java.math.BigInteger;

import cpw.mods.fml.common.Loader;

/**
 * 读取 ProjectE-Team 客户端保留的**精确**团队 EMC 缓存(仅客户端)。
 *
 * <p>
 * ProjectE 的玩家/团队 EMC 在客户端只能是 double,超过 2^53 后显示就是近似的。装了
 * PE-E-GTNH 时,ProjectE-Team 会随 {@code TeamEmcPKT} 额外推来一个十进制字符串(来自服务端
 * BigInteger 团队池),并在客户端主线程 tick 时写进 {@code TeamClientSync.teamEmcBig}。
 * 本类通过反射读它,给转化桌 GUI 的 EMC 行提供逐位精确的值。
 *
 * <p>
 * 全部走反射 + 缓存失败结果:没装 ProjectE-Team、对方版本较旧(没有这个缓存)、或读取失败时
 * 一律返回 null,调用方回退到 double 视图,不会崩。
 */
public final class ProjectETeamClientPX {

    private static final String CLIENT_SYNC = "com.projecte.team.TeamClientSync";

    private static boolean resolved;
    private static Method getTeamEmcBig;

    private ProjectETeamClientPX() {}

    /**
     * @return 精确的团队 EMC;null 表示当前应回退到 double 视图(非 big 模式 / 未收到 / 无对方 mod)
     */
    public static BigInteger getTeamEmcBig() {
        if (!resolved) {
            resolved = true;
            if (Loader.isModLoaded("projecteteam")) {
                try {
                    getTeamEmcBig = Class.forName(CLIENT_SYNC)
                        .getMethod("getTeamEmcBig");
                } catch (Throwable ignored) {
                    // 对方版本较旧:没有这个缓存,保持 null 走 double 视图
                }
            }
        }
        if (getTeamEmcBig == null) {
            return null;
        }
        try {
            return (BigInteger) getTeamEmcBig.invoke(null);
        } catch (Throwable t) {
            return null;
        }
    }
}
