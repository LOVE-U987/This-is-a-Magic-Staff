package com.magicstaff.thisisamagicstaff.item;

import com.magicstaff.thisisamagicstaff.ThisIsAMagicStaff;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 物品转换器
 * 负责将普通物品（如钻石剑）转换为施法物品或恢复为普通物品
 * 通过动态添加/移除 NBT 数据实现（1.20.1 使用 NBT 而非组件系统）
 */
public class ItemTransformer {

    private static final String KEY_IS_TRANSFORMED = "is_transformed";
    private static final String MOD_DATA_KEY = ThisIsAMagicStaff.MODID;
    private static final String KEY_CASTING_IMPLEMENT = "casting_implement";

    /**
     * 将物品转换为施法物品
     *
     * @param itemStack 要转换的物品
     * @param player 玩家
     * @return 是否转换成功
     */
    public static boolean transformToCastingItem(ItemStack itemStack, Player player) {
        // 检查是否已经是施法物品
        if (isCastingItem(itemStack)) {
            ThisIsAMagicStaff.LOGGER.debug("物品已经是施法物品，无需转换");
            return false;
        }

        // 获取或创建 NBT 标签
        CompoundTag tag = itemStack.getOrCreateTag();

        // 添加施法工具标记
        CompoundTag modData = new CompoundTag();
        modData.putBoolean(KEY_CASTING_IMPLEMENT, true);
        tag.put(MOD_DATA_KEY, modData);

        // 添加法术容器（可选，用于存储法术）
        if (!ISpellContainer.isSpellContainer(itemStack)) {
            // 创建空的法术容器
            ISpellContainer spellContainer = ISpellContainer.create(1, true, true);
            // 使用 ISpellContainer.set 方法设置到物品上
            ISpellContainer.set(itemStack, spellContainer);
        }

        // 标记为已转换
        setTransformed(itemStack, true);

        ThisIsAMagicStaff.LOGGER.info("物品 {} 已转换为施法物品", itemStack.getItem().getDescriptionId());
        return true;
    }

    /**
     * 将施法物品恢复为普通物品
     *
     * @param itemStack 要恢复的物品
     * @param player 玩家
     * @return 是否恢复成功
     */
    public static boolean restoreToNormalItem(ItemStack itemStack, Player player) {
        // 检查是否是施法物品
        if (!isCastingItem(itemStack)) {
            ThisIsAMagicStaff.LOGGER.debug("物品不是施法物品，无需恢复");
            return false;
        }

        // 获取 NBT 标签
        CompoundTag tag = itemStack.getTag();
        if (tag != null) {
            // 移除施法工具标记
            if (tag.contains(MOD_DATA_KEY)) {
                CompoundTag modData = tag.getCompound(MOD_DATA_KEY);
                modData.remove(KEY_CASTING_IMPLEMENT);
                if (modData.isEmpty()) {
                    tag.remove(MOD_DATA_KEY);
                }
            }

            // 移除法术容器（如果是我们添加的）
            if (ISpellContainer.isSpellContainer(itemStack)) {
                ISpellContainer.remove(itemStack);
            }
        }

        // 移除转换标记
        setTransformed(itemStack, false);

        ThisIsAMagicStaff.LOGGER.info("物品 {} 已恢复为普通物品", itemStack.getItem().getDescriptionId());
        return true;
    }

    /**
     * 切换物品的施法状态
     *
     * @param itemStack 要切换的物品
     * @param player 玩家
     * @return 切换后的状态（true=施法物品，false=普通物品）
     */
    public static boolean toggleCastingState(ItemStack itemStack, Player player) {
        if (isCastingItem(itemStack)) {
            restoreToNormalItem(itemStack, player);
            return false;
        } else {
            transformToCastingItem(itemStack, player);
            return true;
        }
    }

    /**
     * 检查物品是否是施法物品
     *
     * @param itemStack 物品
     * @return 是否是施法物品
     */
    public static boolean isCastingItem(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        if (tag == null || !tag.contains(MOD_DATA_KEY)) {
            return false;
        }
        CompoundTag modData = tag.getCompound(MOD_DATA_KEY);
        return modData.getBoolean(KEY_CASTING_IMPLEMENT);
    }

    /**
     * 检查物品是否已被转换过
     *
     * @param itemStack 物品
     * @return 是否已转换
     */
    public static boolean isTransformed(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        if (tag == null || !tag.contains(MOD_DATA_KEY)) {
            return false;
        }
        CompoundTag modData = tag.getCompound(MOD_DATA_KEY);
        return modData.getBoolean(KEY_IS_TRANSFORMED);
    }

    /**
     * 设置物品的转换状态
     *
     * @param itemStack 物品
     * @param transformed 是否已转换
     */
    private static void setTransformed(ItemStack itemStack, boolean transformed) {
        CompoundTag tag = itemStack.getOrCreateTag();
        CompoundTag modData;

        if (tag.contains(MOD_DATA_KEY)) {
            modData = tag.getCompound(MOD_DATA_KEY);
        } else {
            modData = new CompoundTag();
        }

        modData.putBoolean(KEY_IS_TRANSFORMED, transformed);
        tag.put(MOD_DATA_KEY, modData);
    }
}
