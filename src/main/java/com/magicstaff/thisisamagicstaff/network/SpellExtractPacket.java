package com.magicstaff.thisisamagicstaff.network;

import com.magicstaff.thisisamagicstaff.item.SpellExtractor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 法术提取网络包
 * 客户端发送请求到服务器执行法术提取
 */
public record SpellExtractPacket() implements CustomPacketPayload {

    public static final Type<SpellExtractPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("this_is_a_magic_staff", "spell_extract")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SpellExtractPacket> STREAM_CODEC =
            StreamCodec.unit(new SpellExtractPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 服务器端处理包
     */
    public static void handle(SpellExtractPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                // 获取主手物品
                ItemStack mainHandItem = serverPlayer.getMainHandItem();

                if (!mainHandItem.isEmpty()) {
                    // 执行法术提取
                    SpellExtractor.extractSpell(mainHandItem, serverPlayer);
                }
            }
        });
    }
}
