
plugins {
    id("com.gtnewhorizons.gtnhconvention")
}

tasks.jar {
    manifest {
        // gtnhgradle 在 usesMixins=true 时会自动把 MixinConfigs 写进 jar manifest,使 mixin
        // 配置在 bootstrap 期(所有 mod,尤其 ProjectE,还没进 classloader)就被处理:
        // 那时目标类探测必然失败,launchwrapper 会把这次失败永久记入负缓存,之后 ProjectE
        // 加载自己的类时直接 NoClassDefFoundError 崩游戏。mixin 配置改由 LateMixinLoader 在
        // FML CONSTRUCTING 阶段(所有 mod jar 已进 classpath)延迟注册,故移除该属性。
        attributes.remove("MixinConfigs")
    }
}
