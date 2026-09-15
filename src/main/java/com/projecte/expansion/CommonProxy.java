package com.projecte.expansion;

import com.projecte.expansion.command.CommandPXReloadEmc;
import com.projecte.expansion.net.PXExactEmcPKT;
import com.projecte.expansion.net.PXLoginHandler;
import com.projecte.expansion.net.PXNetwork;

import cpw.mods.fml.common.FMLCommonHandler;
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
        // 精确 EMC 的 S->C 同步频道:专用服务器上客户端没有精确表,不补这一块就只能显示 int 上限值
        PXNetwork.register();
        FMLCommonHandler.instance()
            .bus()
            .register(new PXLoginHandler());
    }

    /** 客户端由 {@link ClientProxy} 覆写:把服务端同步来的精确值排进主线程队列;服务端不会走到这里。 */
    public void handleExactEmc(PXExactEmcPKT message) {}

    // load "进行 mod 的各项设置。构建你需要的数据结构。注册配方。"
    public void init(FMLInitializationEvent event) {
        // Waila(GTNH 的 HUD,对应上游的 Jade 集成):按类名注册,没装 Waila 时该类不会被加载
        if (Loader.isModLoaded("Waila")) {
            // Waila 的 IMC 字符串格式是 "完整类名.静态方法名"(点分,方法在最后):
            // 它会按最后一个点切分,取前面全部当类名 —— 只给类名会报 Could not find class <包名>
            FMLInterModComms.sendMessage(
                "Waila",
                "register",
                "com.projecte.expansion.integration.WailaProviderPX.callBackRegister");
        }
    }

    // postInit "处理与其他 mod 的交互，并在此基础上完成你的设置。"
    public void postInit(FMLPostInitializationEvent event) {}

    // 在此事件处理器中注册服务端命令
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandPXReloadEmc());
    }
}
