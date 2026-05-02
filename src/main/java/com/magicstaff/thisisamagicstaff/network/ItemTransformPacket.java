package com.magicstaff.thisisamagicstaff.network;

import com.magicstaff.thisisamagicstaff.item.ItemTransformer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 物品转换数据包
 * 客户端发送给服务器，请求转换手持物品
 */
public class ItemTransformPacket {

    private final boolean transformToCasting;

    /**
     * 构造函数
     *
     * @param transformToCasting 是否转换为施法物品
     */
    public ItemTransformPacket(boolean transformToCasting) {
        this.transformToCasting = transformToCasting;
    }

    /**
     * 编码数据包
     *
     * @param packet 数据包
     * @param buffer 缓冲区
     */
    public static void encode(ItemTransformPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.transformToCasting);
    }

    /**
     * 解码数据包
     *
     * @param buffer 缓冲区
     * @return 数据包实例
     */
    public static ItemTransformPacket decode(FriendlyByteBuf buffer) {
        return new ItemTransformPacket(buffer.readBoolean());
    }

    /**
     * 处理数据包
     *
     * @param packet 数据包
     * @param contextSupplier 上下文提供者
     */
    public static void handle(ItemTransformPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer serverPlayer = context.getSender();
            if (serverPlayer == null) {
                return;
            }

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
                        Component.translatable("message.this_is_a_magic_staff.item_transformed_to_casting"),
                        true
                );
            } else {
                serverPlayer.sendSystemMessage(
                        Component.translatable("message.this_is_a_magic_staff.item_restored_to_normal"),
                        true
                );
            }
        });
        context.setPacketHandled(true);
    }
}
