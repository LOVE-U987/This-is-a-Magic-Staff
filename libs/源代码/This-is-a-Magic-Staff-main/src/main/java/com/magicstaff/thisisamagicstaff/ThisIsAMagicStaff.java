package com.magicstaff.thisisamagicstaff;

import com.magicstaff.thisisamagicstaff.network.NetworkHandler;
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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
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
     * 方块注册表
     */
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);

    /**
     * 物品注册表
     */
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    /**
     * 创造模式标签页注册表
     */
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // 创建示例方块
    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));

    // 创建示例方块物品
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);

    // 创建示例物品
    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", new Item.Properties().food(new FoodProperties.Builder()
            .alwaysEdible().nutrition(1).saturationModifier(2f).build()));

    // 创建创造模式标签页
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.this_is_a_magic_staff"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(EXAMPLE_ITEM.get());
            }).build());

    /**
     * 构造函数
     */
    public ThisIsAMagicStaff(IEventBus modEventBus, ModContainer modContainer) {
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
        NeoForge.EVENT_BUS.register(this);

        // 注册创造模式标签页内容事件
        modEventBus.addListener(this::addCreative);

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
