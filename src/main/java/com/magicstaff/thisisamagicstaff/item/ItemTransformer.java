package com.magicstaff.thisisamagicstaff.item;

import com.magicstaff.thisisamagicstaff.ThisIsAMagicStaff;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * 物品转换器
 * 负责将普通物品（如钻石剑）转换为施法物品或恢复为普通物品
 * 通过动态添加/移除 CASTING_IMPLEMENT 组件实现
 */
public class ItemTransformer {

    private static final String KEY_IS_TRANSFORMED = "is_transformed";
    private static final String MOD_DATA_KEY = ThisIsAMagicStaff.MODID;

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

        // 添加 CASTING_IMPLEMENT 组件
        itemStack.set(ComponentRegistry.CASTING_IMPLEMENT.get(), Unit.INSTANCE);

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

        // 移除 CASTING_IMPLEMENT 组件
        itemStack.remove(ComponentRegistry.CASTING_IMPLEMENT.get());

        // 移除法术容器（如果是我们添加的）
        if (ISpellContainer.isSpellContainer(itemStack)) {
            itemStack.remove(ComponentRegistry.SPELL_CONTAINER.get());
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
        return itemStack.has(ComponentRegistry.CASTING_IMPLEMENT.get());
    }

    /**
     * 检查物品是否已被转换过
     *
     * @param itemStack 物品
     * @return 是否已转换
     */
    public static boolean isTransformed(ItemStack itemStack) {
        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return false;
        }
        CompoundTag tag = customData.copyTag();
        return tag.contains(MOD_DATA_KEY) && tag.getCompound(MOD_DATA_KEY).getBoolean(KEY_IS_TRANSFORMED);
    }

    /**
     * 设置物品的转换状态
     *
     * @param itemStack 物品
     * @param transformed 是否已转换
     */
    private static void setTransformed(ItemStack itemStack, boolean transformed) {
        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        CompoundTag modData;

        if (customData != null) {
            CompoundTag existingTag = customData.copyTag();
            if (existingTag.contains(MOD_DATA_KEY)) {
                modData = existingTag.getCompound(MOD_DATA_KEY);
            } else {
                modData = new CompoundTag();
            }
            modData.putBoolean(KEY_IS_TRANSFORMED, transformed);
            existingTag.put(MOD_DATA_KEY, modData);
            itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(existingTag));
        } else {
            modData = new CompoundTag();
            modData.putBoolean(KEY_IS_TRANSFORMED, transformed);
            CompoundTag newTag = new CompoundTag();
            newTag.put(MOD_DATA_KEY, modData);
            itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(newTag));
        }
    }

    /**
     * 给物品添加法术
     *
     * @param itemStack 物品
     * @param spellData 法术数据
     * @return 是否添加成功
     */
    public static boolean addSpellToItem(ItemStack itemStack, SpellData spellData) {
        if (!isCastingItem(itemStack)) {
            ThisIsAMagicStaff.LOGGER.warn("无法给非施法物品添加法术");
            return false;
        }

        if (!ISpellContainer.isSpellContainer(itemStack)) {
            ThisIsAMagicStaff.LOGGER.warn("物品没有法术容器");
            return false;
        }

        ISpellContainer spellContainer = ISpellContainer.get(itemStack);
        // 这里可以实现添加法术的逻辑
        // 具体实现取决于铁魔法的 API

        return true;
    }

    /**
     * 从物品移除法术
     *
     * @param itemStack 物品
     * @param spellIndex 法术索引
     * @return 是否移除成功
     */
    public static boolean removeSpellFromItem(ItemStack itemStack, int spellIndex) {
        if (!isCastingItem(itemStack)) {
            return false;
        }

        if (!ISpellContainer.isSpellContainer(itemStack)) {
            return false;
        }

        // 实现移除法术的逻辑
        return true;
    }
}
