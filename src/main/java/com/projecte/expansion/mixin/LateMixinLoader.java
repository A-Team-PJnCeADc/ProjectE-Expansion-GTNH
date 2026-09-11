package com.projecte.expansion.mixin;

import java.util.Collections;
import java.util.List;

import io.github.tox1cozz.mixinbooterlegacy.ILateMixinLoader;
import io.github.tox1cozz.mixinbooterlegacy.LateMixin;

/**
 * 延迟注册我们的 mixin 配置(GTNH Late mixin 机制),也就是在 FML 的 CONSTRUCTING 阶段注册——
 * 此时所有 mod jar(包括 ProjectE)都已经在 classpath 上。
 *
 * <p>
 * 若改为从 jar manifest 注册,Mixin 会在 bootstrap 期就处理该配置,那时 ProjectE 还无法解析:
 * 目标类会被报告为缺失,launchwrapper 会把这次失败永久缓存,之后 ProjectE 加载自己的类时就会崩
 * (AlchChestTile/TileEmcDirection 上的 NoClassDefFoundError……)。因此 build.gradle.kts 移除了
 * MixinConfigs manifest 属性。
 */
@LateMixin
public class LateMixinLoader implements ILateMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList("mixins.projectexpansiongtnh.json");
    }
}
