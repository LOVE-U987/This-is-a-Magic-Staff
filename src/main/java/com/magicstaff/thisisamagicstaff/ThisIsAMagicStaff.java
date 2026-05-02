package com.magicstaff.thisisamagicstaff;

import com.magicstaff.thisisamagicstaff.network.NetworkHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

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
     * 方块注册表
     */
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);

    /**
     * 物品注册表
     */
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    /**
     * 创造模式标签页注册表
     */
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // 创建示例方块
    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));

    // 创建示例方块物品
    public static final RegistryObject<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block",
            () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties()));

    // 创建示例物品
    public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register("example_item",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                    .alwaysEat().nutrition(1).saturationMod(2f).build())));

    // 创建创造模式标签页
    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.this_is_a_magic_staff"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(EXAMPLE_ITEM.get());
                    }).build());

    /**
     * 构造函数
     */
    public ThisIsAMagicStaff() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 注册通用设置事件
        modEventBus.addListener(this::commonSetup);

        // 注册方块
        BLOCKS.register(modEventBus);

        // 注册物品
        ITEMS.register(modEventBus);

        // 注册创造模式标签页
        CREATIVE_MODE_TABS.register(modEventBus);

        // 注册网络处理器
        NetworkHandler.register(modEventBus);

        // 注册事件总线
        MinecraftForge.EVENT_BUS.register(this);

        // 注册创造模式标签页内容事件
        modEventBus.addListener(this::addCreative);

        // 注册配置文件
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    /**
     * 通用设置
     */
    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("This is a Magic Staff! 模组加载中...");
        LOGGER.info("物品转换系统已初始化");
    }

    /**
     * 添加创造模式物品
     */
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(EXAMPLE_BLOCK_ITEM);
        }
    }

    /**
     * 服务器启动事件
     */
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("This is a Magic Staff! 服务器启动");
    }
}
