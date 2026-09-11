package com.projecte.expansion;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.projecte.expansion.registry.PXContent;
import com.projecte.expansion.registry.PXEmc;
import com.projecte.expansion.registry.PXFuels;
import com.projecte.expansion.registry.PXRecipes;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(
    modid = MyMod.MODID,
    version = Tags.VERSION,
    name = "ProjectE-Expansion-GTNH",
    acceptedMinecraftVersions = "[1.7.10]")
public class MyMod {

    public static final String MODID = "projectexpansiongtnh";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @SidedProxy(clientSide = "com.projecte.expansion.ClientProxy", serverSide = "com.projecte.expansion.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    // preInit "在做任何其他事情之前运行。读取你的配置，创建方块、物品等，并将它们注册到
    // GameRegistry。"
    public void preInit(FMLPreInitializationEvent event) {
        Config.load(event.getSuggestedConfigurationFile());
        PXContent.register();
        proxy.preInit(event);
    }

    @Mod.EventHandler
    // load "进行 mod 的各项设置。构建你需要的数据结构。注册配方。"
    public void init(FMLInitializationEvent event) {
        // 配方放在 init（而不是 preInit）中：ProjectE 的方块物品（收集器 mk1 等）只有在
        // ProjectE 自己的 preInit 注册完方块之后才能解析出来
        PXRecipes.register();
        PXFuels.register();
        proxy.init(event);
    }

    @Mod.EventHandler
    // postInit "处理与其他 mod 的交互，并在此基础上完成你的设置。"
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    // 在此事件处理器中注册服务端命令
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        PXEmc.registerValues();
    }
}
