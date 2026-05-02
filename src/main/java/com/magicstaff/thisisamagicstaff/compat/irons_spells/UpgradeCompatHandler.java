package com.magicstaff.thisisamagicstaff.compat.irons_spells;

import com.magicstaff.thisisamagicstaff.Config;
import com.magicstaff.thisisamagicstaff.ThisIsAMagicStaff;
import com.magicstaff.thisisamagicstaff.item.ItemTransformer;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * 铁魔法升级法球兼容性处理器
 * 使被转换的物品可以被升级法球加成
 *
 * 工作原理：
 * 1. 通过反射获取铁魔法的 UPGRADE_WHITELIST_ITEMS 集合
 * 2. 当物品被转换时，将物品类型添加到这个集合中
 * 3. 这样铁魔法的 canBeUpgraded 检查就会通过
 */
public class UpgradeCompatHandler {

    private static Set<Item> upgradeWhitelistItems = null;
    private static boolean reflectionFailed = false;

    /**
     * 获取铁魔法的升级白名单集合
     */
    @SuppressWarnings("unchecked")
    private static Set<Item> getUpgradeWhitelist() {
        if (upgradeWhitelistItems != null) {
            return upgradeWhitelistItems;
        }
        if (reflectionFailed) {
            return null;
        }

        try {
            Field field = ServerConfigs.class.getDeclaredField("UPGRADE_WHITELIST_ITEMS");
            field.setAccessible(true);
            upgradeWhitelistItems = (Set<Item>) field.get(null);
            return upgradeWhitelistItems;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            ThisIsAMagicStaff.LOGGER.error("无法通过反射获取铁魔法的升级白名单: {}", e.getMessage());
            reflectionFailed = true;
            return null;
        }
    }

    /**
     * 确保转换物品具有升级兼容性
     * 在物品被转换时调用
     *
     * @param itemStack 被转换的物品
     */
    public static void ensureUpgradeable(ItemStack itemStack) {
        if (!Config.ENABLE_UPGRADE_COMPAT.get()) {
            return;
        }

        // 将物品添加到铁魔法的升级白名单中
        // 注意：不要添加 UPGRADE_DATA 组件，否则铁魔法会在物品名称后显示 +0
        Set<Item> whitelist = getUpgradeWhitelist();
        if (whitelist != null) {
            whitelist.add(itemStack.getItem());
        }
    }

    /**
     * 从升级白名单中移除物品
     * 在物品被恢复时调用
     *
     * @param itemStack 被恢复的物品
     */
    public static void removeUpgradeable(ItemStack itemStack) {
        if (!Config.ENABLE_UPGRADE_COMPAT.get()) {
            return;
        }

        Set<Item> whitelist = getUpgradeWhitelist();
        if (whitelist != null) {
            whitelist.remove(itemStack.getItem());
        }
    }

    /**
     * 检查物品是否可以被升级法球加成
     *
     * @param itemStack 物品
     * @return 是否可以被升级
     */
    public static boolean canBeUpgraded(ItemStack itemStack) {
        if (!Config.ENABLE_UPGRADE_COMPAT.get()) {
            return false;
        }

        return ItemTransformer.isCastingItem(itemStack);
    }
}
