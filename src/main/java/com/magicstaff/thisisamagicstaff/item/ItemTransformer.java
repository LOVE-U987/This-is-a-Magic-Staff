package com.magicstaff.thisisamagicstaff.item;

import com.magicstaff.thisisamagicstaff.Config;
import com.magicstaff.thisisamagicstaff.ThisIsAMagicStaff;
import com.magicstaff.thisisamagicstaff.compat.irons_spells.UpgradeCompatHandler;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellSlot;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

/**
 * 物品转换器
 * 负责将普通物品（如钻石剑）转换为施法物品或恢复为普通物品
 * 通过动态添加/移除 CASTING_IMPLEMENT 组件实现
 *
 * 重要：会保存和恢复物品原有的法术数据，避免数据丢失
 */
public class ItemTransformer {

    private static final String KEY_IS_TRANSFORMED = "is_transformed";
    private static final String KEY_SAVED_SPELLS = "saved_spells";
    private static final String KEY_SPELL_MAX_SLOTS = "max_slots";
    private static final String KEY_SPELL_WHEEL = "spell_wheel";
    private static final String KEY_SPELL_MUST_EQUIP = "must_equip";
    private static final String KEY_SPELL_IMPROVED = "improved";
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

        // 检查是否有保存的法术数据需要恢复
        if (hasSavedSpells(itemStack)) {
            // 恢复之前保存的法术
            restoreSpells(itemStack);
        } else {
            // 检查物品是否已有法术容器（例如原本就是法杖）
            if (!ISpellContainer.isSpellContainer(itemStack)) {
                // 创建空的法术容器
                ISpellContainer spellContainer = ISpellContainer.create(1, true, true);
                ISpellContainer.set(itemStack, spellContainer);
            }
            // 如果已有法术容器，保留原有的法术
        }

        // 标记为已转换
        setTransformed(itemStack, true);

        // 确保升级兼容性
        UpgradeCompatHandler.ensureUpgradeable(itemStack);

        if (Config.LOG_DIRT_BLOCK.get()) {
            ThisIsAMagicStaff.LOGGER.info("物品 {} 已转换为施法物品", itemStack.getItem().getDescriptionId());
        }
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

        // 保存现有的法术数据（如果有）
        if (ISpellContainer.isSpellContainer(itemStack)) {
            ISpellContainer container = ISpellContainer.get(itemStack);
            if (container != null && !container.isEmpty()) {
                saveSpells(itemStack, container);
            }
        }

        // 移除 CASTING_IMPLEMENT 组件
        itemStack.remove(ComponentRegistry.CASTING_IMPLEMENT.get());

        // 移除法术容器
        if (ISpellContainer.isSpellContainer(itemStack)) {
            itemStack.remove(ComponentRegistry.SPELL_CONTAINER.get());
        }

        // 移除转换标记（但保留保存的法术数据）
        setTransformed(itemStack, false);

        // 移除升级兼容性
        UpgradeCompatHandler.removeUpgradeable(itemStack);

        if (Config.LOG_DIRT_BLOCK.get()) {
            ThisIsAMagicStaff.LOGGER.info("物品 {} 已恢复为普通物品（法术数据已保存）", itemStack.getItem().getDescriptionId());
        }
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
     * 保存法术数据到物品的自定义NBT中
     *
     * @param itemStack 物品
     * @param container 法术容器
     */
    private static void saveSpells(ItemStack itemStack, ISpellContainer container) {
        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        CompoundTag modData;
        CompoundTag existingTag;

        if (customData != null) {
            existingTag = customData.copyTag();
            if (existingTag.contains(MOD_DATA_KEY)) {
                modData = existingTag.getCompound(MOD_DATA_KEY);
            } else {
                modData = new CompoundTag();
            }
        } else {
            existingTag = new CompoundTag();
            modData = new CompoundTag();
        }

        // 保存法术容器配置
        modData.putInt(KEY_SPELL_MAX_SLOTS, container.getMaxSpellCount());
        modData.putBoolean(KEY_SPELL_WHEEL, container.isSpellWheel());
        modData.putBoolean(KEY_SPELL_MUST_EQUIP, container.mustEquip());
        modData.putBoolean(KEY_SPELL_IMPROVED, container.isImproved());

        // 保存法术列表
        ListTag spellsList = new ListTag();
        List<SpellSlot> activeSpells = container.getActiveSpells();
        for (SpellSlot slot : activeSpells) {
            CompoundTag spellTag = new CompoundTag();
            spellTag.putString("spell_id", slot.getSpell().getSpellResource().toString());
            spellTag.putInt("level", slot.getLevel());
            spellTag.putInt("index", slot.index());
            spellTag.putBoolean("locked", slot.isLocked());
            spellsList.add(spellTag);
        }
        modData.put(KEY_SAVED_SPELLS, spellsList);

        existingTag.put(MOD_DATA_KEY, modData);
        itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(existingTag));

        ThisIsAMagicStaff.LOGGER.debug("已保存 {} 个法术到物品", activeSpells.size());
    }

    /**
     * 从物品的自定义NBT中恢复法术数据
     *
     * @param itemStack 物品
     */
    private static void restoreSpells(ItemStack itemStack) {
        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return;
        }

        CompoundTag tag = customData.copyTag();
        if (!tag.contains(MOD_DATA_KEY)) {
            return;
        }

        CompoundTag modData = tag.getCompound(MOD_DATA_KEY);
        if (!modData.contains(KEY_SAVED_SPELLS)) {
            return;
        }

        // 读取法术容器配置
        int maxSlots = modData.getInt(KEY_SPELL_MAX_SLOTS);
        boolean spellWheel = modData.getBoolean(KEY_SPELL_WHEEL);
        boolean mustEquip = modData.getBoolean(KEY_SPELL_MUST_EQUIP);
        boolean improved = modData.getBoolean(KEY_SPELL_IMPROVED);

        // 创建可变的法术容器
        var mutableContainer = ISpellContainer.create(maxSlots, spellWheel, mustEquip).mutableCopy();
        mutableContainer.setImproved(improved);

        // 恢复法术
        ListTag spellsList = modData.getList(KEY_SAVED_SPELLS, Tag.TAG_COMPOUND);
        for (int i = 0; i < spellsList.size(); i++) {
            CompoundTag spellTag = spellsList.getCompound(i);
            String spellId = spellTag.getString("spell_id");
            int level = spellTag.getInt("level");
            int index = spellTag.getInt("index");
            boolean locked = spellTag.getBoolean("locked");

            // 通过法术ID获取法术并添加
            io.redspace.ironsspellbooks.api.spells.AbstractSpell spell =
                io.redspace.ironsspellbooks.api.registry.SpellRegistry.getSpell(
                    net.minecraft.resources.ResourceLocation.parse(spellId)
                );

            if (spell != null) {
                mutableContainer.addSpellAtIndex(spell, level, index, locked);
            }
        }

        // 设置到物品上
        ISpellContainer.set(itemStack, mutableContainer.toImmutable());

        ThisIsAMagicStaff.LOGGER.debug("已恢复 {} 个法术到物品", spellsList.size());
    }

    /**
     * 检查物品是否有保存的法术数据
     *
     * @param itemStack 物品
     * @return 是否有保存的法术
     */
    private static boolean hasSavedSpells(ItemStack itemStack) {
        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return false;
        }
        CompoundTag tag = customData.copyTag();
        if (!tag.contains(MOD_DATA_KEY)) {
            return false;
        }
        CompoundTag modData = tag.getCompound(MOD_DATA_KEY);
        return modData.contains(KEY_SAVED_SPELLS) && !modData.getList(KEY_SAVED_SPELLS, Tag.TAG_COMPOUND).isEmpty();
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
        CompoundTag existingTag;

        if (customData != null) {
            existingTag = customData.copyTag();
            if (existingTag.contains(MOD_DATA_KEY)) {
                modData = existingTag.getCompound(MOD_DATA_KEY);
            } else {
                modData = new CompoundTag();
            }
        } else {
            existingTag = new CompoundTag();
            modData = new CompoundTag();
        }

        modData.putBoolean(KEY_IS_TRANSFORMED, transformed);
        existingTag.put(MOD_DATA_KEY, modData);
        itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(existingTag));
    }
}
