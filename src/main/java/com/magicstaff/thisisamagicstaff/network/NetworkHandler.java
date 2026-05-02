package com.magicstaff.thisisamagicstaff.network;

import com.magicstaff.thisisamagicstaff.ThisIsAMagicStaff;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

/**
 * 网络处理器
 * 注册所有网络数据包（Forge 版本）
 */
public class NetworkHandler {

    private static final String PROTOCOL_VERSION = "1.0";
    private static final ResourceLocation CHANNEL_NAME = new ResourceLocation(ThisIsAMagicStaff.MODID, "main");

    /**
     * SimpleChannel 用于 Forge 网络通信
     */
    public static SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            CHANNEL_NAME,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    /**
     * 获取下一个数据包ID
     */
    public static int nextId() {
        return packetId++;
    }

    /**
     * 注册所有网络数据包
     */
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(NetworkHandler::registerPackets);
    }

    /**
     * 注册数据包
     */
    private static void registerPackets(net.minecraftforge.eventbus.api.IEventBus event) {
        // 注册物品转换数据包（发送到服务器）
        CHANNEL.registerMessage(
                nextId(),
                ItemTransformPacket.class,
                ItemTransformPacket::encode,
                ItemTransformPacket::decode,
                ItemTransformPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
    }
}
