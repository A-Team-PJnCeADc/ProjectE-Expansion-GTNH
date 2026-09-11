package com.projecte.expansion;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class CommonProxy {

    // preInit "在做任何其他事情之前运行。读取你的配置，创建方块、物品等，并将它们注册到
    // GameRegistry。"
    public void preInit(FMLPreInitializationEvent event) {
        MyMod.LOG.info("ProjectE-Expansion-GTNH at version " + Tags.VERSION);
    }

    // load "进行 mod 的各项设置。构建你需要的数据结构。注册配方。"
    public void init(FMLInitializationEvent event) {
        // Waila(GTNH 的 HUD,对应上游的 Jade 集成):按类名注册,没装 Waila 时该类不会被加载
        if (Loader.isModLoaded("Waila")) {
            FMLInterModComms.sendMessage("Waila", "register", "com.projecte.expansion.integration.WailaProviderPX");
        }
    }

    // postInit "处理与其他 mod 的交互，并在此基础上完成你的设置。"
    public void postInit(FMLPostInitializationEvent event) {}

    // 在此事件处理器中注册服务端命令
    public void serverStarting(FMLServerStartingEvent event) {}
}
