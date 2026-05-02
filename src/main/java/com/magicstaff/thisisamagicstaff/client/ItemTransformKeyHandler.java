package com.magicstaff.thisisamagicstaff.client;

import org.lwjgl.glfw.GLFW;

import com.magicstaff.thisisamagicstaff.ThisIsAMagicStaff;
import com.magicstaff.thisisamagicstaff.item.SpellExtractor;
import com.magicstaff.thisisamagicstaff.network.ItemTransformPacket;
import com.magicstaff.thisisamagicstaff.network.SpellExtractPacket;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 物品转换按键处理器
 * 处理将手持物品转换为施法物品或恢复为普通物品的按键
 */
@EventBusSubscriber(modid = ThisIsAMagicStaff.MODID, value = Dist.CLIENT)
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
     * 提取法术按键（默认 J 键）
     */
    public static final KeyMapping EXTRACT_SPELL_KEY = new KeyMapping(
            "key.this_is_a_magic_staff.extract_spell",
            GLFW.GLFW_KEY_J,
            "key.categories.this_is_a_magic_staff"
    );

    /**
     * 注册按键绑定
     */
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TRANSFORM_ITEM_KEY);
        event.register(EXTRACT_SPELL_KEY);
    }

    /**
     * 处理按键输入
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        // 只在游戏进行中处理按键
        if (minecraft.player == null) return;

        // 处理转换物品按键
        while (TRANSFORM_ITEM_KEY.consumeClick()) {
            transformHeldItem();
        }

        // 处理提取法术按键
        while (EXTRACT_SPELL_KEY.consumeClick()) {
            extractSpellFromItem();
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
        PacketDistributor.sendToServer(new ItemTransformPacket(true));
    }

    /**
     * 从手持物品中提取法术
     */
    private static void extractSpellFromItem() {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;

        // 获取主手物品
        ItemStack mainHandItem = player.getMainHandItem();

        if (mainHandItem.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.this_is_a_magic_staff.no_item_in_hand"));
            return;
        }

        // 检查是否可以提取法术
        if (!SpellExtractor.canExtractSpell(mainHandItem)) {
            player.sendSystemMessage(Component.translatable("message.this_is_a_magic_staff.no_spells"));
            return;
        }

        // 发送网络包到服务器执行提取
        PacketDistributor.sendToServer(new SpellExtractPacket());
    }
}
