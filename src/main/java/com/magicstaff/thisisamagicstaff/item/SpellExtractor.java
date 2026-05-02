package com.magicstaff.thisisamagicstaff.item;

import com.magicstaff.thisisamagicstaff.Config;
import com.magicstaff.thisisamagicstaff.ThisIsAMagicStaff;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellSlot;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 法术提取器
 * 负责从法术书或武器中提取法术，生成法术卷轴
 * 提取操作会消耗经验值
 */
public class SpellExtractor {

    /**
     * 从物品中提取第一个法术
     *
     * @param itemStack 包含法术的物品
     * @param player 玩家
     * @return 是否提取成功
     */
    public static boolean extractSpell(ItemStack itemStack, Player player) {
        // 检查功能是否启用
        if (!Config.ENABLE_SPELL_EXTRACTION.get()) {
            if (!player.level().isClientSide) {
                player.sendSystemMessage(Component.translatable("message.this_is_a_magic_staff.extraction_disabled"));
            }
            return false;
        }

        // 检查物品是否有法术容器
        if (!ISpellContainer.isSpellContainer(itemStack)) {
            if (!player.level().isClientSide) {
                player.sendSystemMessage(Component.translatable("message.this_is_a_magic_staff.no_spells"));
            }
            return false;
        }

        ISpellContainer container = ISpellContainer.get(itemStack);
        if (container == null || container.isEmpty()) {
            if (!player.level().isClientSide) {
                player.sendSystemMessage(Component.translatable("message.this_is_a_magic_staff.no_spells"));
            }
            return false;
        }

        // 获取第一个法术
        List<SpellSlot> activeSpells = container.getActiveSpells();
        if (activeSpells.isEmpty()) {
            if (!player.level().isClientSide) {
                player.sendSystemMessage(Component.translatable("message.this_is_a_magic_staff.no_spells"));
            }
            return false;
        }

        SpellSlot spellSlot = activeSpells.get(0);

        // 检查玩家是否有足够的经验
        int requiredLevel = Config.SPELL_EXTRACTION_COST.get();
        if (player.experienceLevel < requiredLevel) {
            if (!player.level().isClientSide) {
                player.sendSystemMessage(Component.translatable(
                    "message.this_is_a_magic_staff.insufficient_xp",
                    requiredLevel
                ));
            }
            return false;
        }

        // 在服务端执行提取
        if (!player.level().isClientSide) {
            ServerPlayer serverPlayer = (ServerPlayer) player;

            // 创建法术卷轴
            ItemStack scrollStack = new ItemStack(ItemRegistry.SCROLL.get());
            ISpellContainer.createScrollContainer(spellSlot.getSpell(), spellSlot.getLevel(), scrollStack);

            // 将卷轴添加到玩家背包
            if (!serverPlayer.getInventory().add(scrollStack)) {
                // 背包满了，掉落物品
                serverPlayer.drop(scrollStack, false);
            }

            // 从原物品中移除法术
            var mutableContainer = container.mutableCopy();
            mutableContainer.removeSpellAtIndex(spellSlot.index());
            ISpellContainer.set(itemStack, mutableContainer.toImmutable());

            // 消耗经验值
            serverPlayer.giveExperienceLevels(-requiredLevel);

            // 播放音效
            serverPlayer.level().playSound(
                null,
                serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.PLAYERS,
                1.0f,
                1.0f
            );

            // 发送成功消息
            serverPlayer.sendSystemMessage(Component.translatable(
                "message.this_is_a_magic_staff.spell_extracted",
                spellSlot.getSpell().getDisplayName(serverPlayer),
                requiredLevel
            ));

            ThisIsAMagicStaff.LOGGER.info(
                "玩家 {} 从 {} 中提取了法术 {}（等级 {}），消耗 {} 级经验",
                serverPlayer.getName().getString(),
                itemStack.getItem().getDescriptionId(),
                spellSlot.getSpell().getSpellResource(),
                spellSlot.getLevel(),
                requiredLevel
            );
        }

        return true;
    }

    /**
     * 检查物品是否可以提取法术
     *
     * @param itemStack 物品
     * @return 是否可以提取
     */
    public static boolean canExtractSpell(ItemStack itemStack) {
        if (!Config.ENABLE_SPELL_EXTRACTION.get()) {
            return false;
        }

        if (!ISpellContainer.isSpellContainer(itemStack)) {
            return false;
        }

        ISpellContainer container = ISpellContainer.get(itemStack);
        return container != null && !container.isEmpty();
    }
}
