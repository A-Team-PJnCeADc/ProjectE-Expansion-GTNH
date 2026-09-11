package com.projecte.expansion.registry;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 用于运行时 ProjectE 补丁（mixin）的一次性日志标记。每个注入的处理器在首次运行时调用
 * {@link #mark(String)}，因此仅凭游戏日志就能看出该环境下哪些价格点确实被打了补丁
 * （正式发布版 jar 使用 SRG 名，开发工作区使用 MCP 名，因此静默未生效才是值得记录的故障
 * 模式）。
 */
public final class PXPatchLog {

    private static final Logger LOG = LogManager.getLogger("projectexpansiongtnh");
    private static final Set<String> SEEN = Collections.synchronizedSet(new HashSet<String>());

    private PXPatchLog() {}

    public static void mark(String patch) {
        if (SEEN.add(patch)) {
            LOG.info("PX patch active: {}", patch);
        }
    }
}
