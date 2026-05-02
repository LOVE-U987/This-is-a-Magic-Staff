package com.magicstaff.thisisamagicstaff.client;

import com.magicstaff.thisisamagicstaff.ThisIsAMagicStaff;
import com.magicstaff.thisisamagicstaff.network.ItemTransformPacket;
import com.magicstaff.thisisamagicstaff.network.NetworkHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * 物品转换按键处理器
 * 处理将手持物品转换为施法物品或恢复为普通物品的按键
 */
@Mod.EventBusSubscriber(modid = ThisIsAMagicStaff.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ItemTransformKeyHandler {

    /**
     * 转换物品按键（默认 H 键）
     */
    public static final KeyMapping TRANSFORM_ITEM_KEY = new KeyMapping(
            "key.this_is_a_magic_staff.transform_item",
            GLFW.GLFW_KEY_H,
            "key.categories.this_is_a_magic_staff"
    );

    /**
     * 注册按键绑定
     */
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TRANSFORM_ITEM_KEY);
    }

    /**
     * 客户端 tick 处理器类
     */
    @Mod.EventBusSubscriber(modid = ThisIsAMagicStaff.MODID, value = Dist.CLIENT)
    public static class ClientTickHandler {

        /**
         * 处理客户端 tick 事件
         */
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }

            Minecraft minecraft = Minecraft.getInstance();

            // 只在游戏进行中处理按键
            if (minecraft.player == null) return;

            // 处理转换物品按键
            while (TRANSFORM_ITEM_KEY.consumeClick()) {
                transformHeldItem();
            }
        }

        /**
         * 转换手持物品
         */
        private static void transformHeldItem() {
            Minecraft minecraft = Minecraft.getInstance();
            Player player = minecraft.player;
            if (player == null) return;

            // 获取主手物品
            ItemStack mainHandItem = player.getMainHandItem();

            if (mainHandItem.isEmpty()) {
                player.sendSystemMessage(Component.translatable("message.this_is_a_magic_staff.no_item_in_hand"));
                return;
            }

            // 发送网络包到服务器执行转换
            NetworkHandler.CHANNEL.sendToServer(new ItemTransformPacket(true));
        }
    }
}
