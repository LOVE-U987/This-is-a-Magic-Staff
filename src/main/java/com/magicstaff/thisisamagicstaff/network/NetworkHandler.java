package com.magicstaff.thisisamagicstaff.network;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 网络处理器
 * 注册所有网络数据包
 */
public class NetworkHandler {

    private static final String PROTOCOL_VERSION = "1.0";

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(NetworkHandler::registerPackets);
    }

    private static void registerPackets(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        // 注册物品转换数据包
        registrar.playToServer(
                ItemTransformPacket.TYPE,
                ItemTransformPacket.STREAM_CODEC,
                ItemTransformPacket::handle
        );

        // 注册法术提取数据包
        registrar.playToServer(
                SpellExtractPacket.TYPE,
                SpellExtractPacket.STREAM_CODEC,
                SpellExtractPacket::handle
        );
    }
}
