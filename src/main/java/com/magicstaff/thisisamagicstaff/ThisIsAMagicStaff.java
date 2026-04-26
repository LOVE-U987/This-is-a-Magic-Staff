package com.magicstaff.thisisamagicstaff;

import com.magicstaff.thisisamagicstaff.network.NetworkHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * This is a Magic Staff! 主类
 * 提供物品动态转换功能，可将任何物品转换为施法物品
 */
@Mod(ThisIsAMagicStaff.MODID)
public class ThisIsAMagicStaff {
    /**
     * 模组ID
     */
    public static final String MODID = "this_is_a_magic_staff";

    /**
     * 日志记录器
     */
    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * 构造函数
     */
    public ThisIsAMagicStaff(IEventBus modEventBus, ModContainer modContainer) {
        // 注册通用设置事件
        modEventBus.addListener(this::commonSetup);

        // 注册网络处理器
        NetworkHandler.register(modEventBus);

        // 注册事件总线
        NeoForge.EVENT_BUS.register(this);

        // 注册配置文件
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    /**
     * 通用设置
     */
    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("This is a Magic Staff! 模组加载中...");
        LOGGER.info("物品转换系统已初始化");
    }

    /**
     * 服务器启动事件
     */
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("This is a Magic Staff! 服务器启动");
    }
}
