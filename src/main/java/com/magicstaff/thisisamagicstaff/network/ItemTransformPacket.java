package com.magicstaff.thisisamagicstaff.network;

import com.magicstaff.thisisamagicstaff.ThisIsAMagicStaff;
import com.magicstaff.thisisamagicstaff.item.ItemTransformer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 物品转换数据包
 * 客户端发送给服务器，请求转换手持物品
 */
public record ItemTransformPacket(boolean transformToCasting) implements CustomPacketPayload {

    public static final Type<ItemTransformPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ThisIsAMagicStaff.MODID, "item_transform")
    );

    public static final StreamCodec<FriendlyByteBuf, ItemTransformPacket> STREAM_CODEC = StreamCodec.composite(
            net.minecraft.network.codec.ByteBufCodecs.BOOL,
            ItemTransformPacket::transformToCasting,
            ItemTransformPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 服务器端处理
     */
    public static void handle(ItemTransformPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                // 获取主手物品
                ItemStack mainHandItem = serverPlayer.getMainHandItem();

                if (mainHandItem.isEmpty()) {
                    return;
                }

                // 执行转换
                boolean isNowCasting;
                if (ItemTransformer.isCastingItem(mainHandItem)) {
                    // 当前是施法物品，恢复为普通物品
                    ItemTransformer.restoreToNormalItem(mainHandItem, serverPlayer);
                    isNowCasting = false;
                } else {
                    // 当前是普通物品，转换为施法物品
                    ItemTransformer.transformToCastingItem(mainHandItem, serverPlayer);
                    isNowCasting = true;
                }

                // 发送消息给玩家
                if (isNowCasting) {
                    serverPlayer.sendSystemMessage(
                            net.minecraft.network.chat.Component.translatable("message.this_is_a_magic_staff.item_transformed_to_casting"),
                            true
                    );
                } else {
                    serverPlayer.sendSystemMessage(
                            net.minecraft.network.chat.Component.translatable("message.this_is_a_magic_staff.item_restored_to_normal"),
                            true
                    );
                }
            }
        });
    }
}
