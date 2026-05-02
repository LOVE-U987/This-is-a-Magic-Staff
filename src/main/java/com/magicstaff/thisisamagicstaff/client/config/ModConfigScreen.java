package com.magicstaff.thisisamagicstaff.client.config;

import com.magicstaff.thisisamagicstaff.Config;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * 模组配置界面
 * 提供可视化的配置选项，带有丰富的动画效果
 * 包含多种进入动画、悬停效果和交互反馈
 */
@OnlyIn(Dist.CLIENT)
public class ModConfigScreen extends Screen {

    private final Screen parent;
    private final List<ConfigWidget> configWidgets = new ArrayList<>();
    private final Random random = new Random();

    // 布局常量
    private static final int PADDING = 24;
    private static final int ROW_HEIGHT = 32;
    private static final int BUTTON_WIDTH = 120;
    private static final int LABEL_WIDTH = 200;

    // 动画相关 - 打开动画
    private float openAnimationProgress = 0.0f;
    private long animationStartTime = 0;
    private boolean firstOpen = true;
    private int entryIndex = 0;

    // 动画相关 - 悬停效果
    private int hoveredEntryIndex = -1;
    private float[] hoverProgress;

    // 动画相关 - 背景粒子
    private List<Particle> particles = new ArrayList<>();
    private long lastParticleSpawn = 0;

    // 动画类型枚举
    private enum EntryAnimationType {
        SLIDE_FROM_RIGHT,   // 从右侧滑入
        SLIDE_FROM_LEFT,    // 从左侧滑入
        SCALE_UP,           // 缩放进入
        FADE_IN,            // 淡入
        BOUNCE,             // 弹跳进入
        FLIP,               // 翻转进入
        SLIDE_FROM_BOTTOM   // 从底部滑入
    }

    private EntryAnimationType[] entryAnimationTypes;

    // 滚动相关
    private int scrollOffset = 0;
    private int totalContentHeight = 0;
    private boolean isScrolling = false;
    private float scrollVelocity = 0;

    // 面板尺寸
    private int panelTop;
    private int panelBottom;
    private int panelLeft;
    private int panelRight;
    private int panelWidth;
    private int panelHeight;

    // 标题动画
    private float titleGlow = 0.0f;
    private boolean titleGlowIncreasing = true;

    public ModConfigScreen(Screen parent) {
        super(Component.translatable("this_is_a_magic_staff.config.title"));
        this.parent = parent;
        this.animationStartTime = System.currentTimeMillis();
    }

    @Override
    protected void init() {
        super.init();
        this.configWidgets.clear();
        this.entryIndex = 0;
        this.particles.clear();

        // 计算面板尺寸
        this.panelWidth = Math.min(600, this.width - 40);
        this.panelHeight = Math.min(420, this.height - 80);
        this.panelLeft = (this.width - panelWidth) / 2;
        this.panelRight = panelLeft + panelWidth;
        this.panelTop = (this.height - panelHeight) / 2;
        this.panelBottom = panelTop + panelHeight;

        // 为每个配置项随机分配动画类型
        entryAnimationTypes = new EntryAnimationType[10];
        EntryAnimationType[] types = EntryAnimationType.values();
        for (int i = 0; i < entryAnimationTypes.length; i++) {
            entryAnimationTypes[i] = types[random.nextInt(types.length)];
        }

        // 初始化悬停进度数组
        hoverProgress = new float[10];

        int currentY = panelTop + 55;

        // 添加配置项：调试模式
        currentY = addBooleanConfigRow(
            currentY,
            "this_is_a_magic_staff.config.debug_mode",
            "this_is_a_magic_staff.config.debug_mode.tooltip",
            Config.LOG_DIRT_BLOCK.get(),
            value -> Config.LOG_DIRT_BLOCK.set(value)
        );

        // 添加配置项：魔法数字
        currentY = addIntConfigRow(
            currentY,
            "this_is_a_magic_staff.config.magic_number",
            "this_is_a_magic_staff.config.magic_number.tooltip",
            Config.MAGIC_NUMBER.get(),
            0, Integer.MAX_VALUE,
            value -> Config.MAGIC_NUMBER.set(value)
        );

        // 添加配置项：物品列表
        currentY = addStringConfigRow(
            currentY,
            "this_is_a_magic_staff.config.items",
            "this_is_a_magic_staff.config.items.tooltip",
            String.join(", ", Config.ITEM_STRINGS.get()),
            value -> {
                List<String> list = new ArrayList<>();
                for (String s : value.split(",")) {
                    String trimmed = s.trim();
                    if (!trimmed.isEmpty()) {
                        list.add(trimmed);
                    }
                }
                Config.ITEM_STRINGS.set(list);
            }
        );

        // 添加配置项：升级法球兼容性
        currentY = addBooleanConfigRow(
            currentY,
            "this_is_a_magic_staff.config.upgrade_compat",
            "this_is_a_magic_staff.config.upgrade_compat.tooltip",
            Config.ENABLE_UPGRADE_COMPAT.get(),
            value -> Config.ENABLE_UPGRADE_COMPAT.set(value)
        );

        this.totalContentHeight = currentY - (panelTop + 55);

        // 添加完成按钮 - 使用 addRenderableWidget 确保交互正常
        this.addRenderableWidget(Button.builder(
                CommonComponents.GUI_DONE,
                button -> this.onClose()
            )
            .bounds(this.width / 2 - 100, this.height - 35, 200, 22)
            .build()
        );
    }

    /**
     * 添加布尔类型配置行
     */
    private int addBooleanConfigRow(int y, String labelKey, String tooltipKey, boolean initialValue, Consumer<Boolean> onChange) {
        int widgetIndex = entryIndex++;

        CycleButton<Boolean> button = CycleButton.booleanBuilder(
                Component.translatable("this_is_a_magic_staff.config.value.on"),
                Component.translatable("this_is_a_magic_staff.config.value.off")
            )
            .displayOnlyValue()
            .withInitialValue(initialValue)
            .create(panelRight - PADDING - BUTTON_WIDTH, y, BUTTON_WIDTH, 20, Component.empty(),
                (btn, value) -> onChange.accept(value));

        button.setTooltip(Tooltip.create(Component.translatable(tooltipKey)));

        // 添加到可渲染列表以确保交互正常
        this.addRenderableWidget(button);

        configWidgets.add(new ConfigWidget(
            Component.translatable(labelKey),
            button,
            widgetIndex
        ));

        return y + ROW_HEIGHT;
    }

    /**
     * 添加整数类型配置行
     */
    private int addIntConfigRow(int y, String labelKey, String tooltipKey, int initialValue, int min, int max, Consumer<Integer> onChange) {
        int widgetIndex = entryIndex++;

        EditBox editBox = new EditBox(this.minecraft.font, panelRight - PADDING - BUTTON_WIDTH, y, BUTTON_WIDTH, 20, Component.empty());
        editBox.setValue(String.valueOf(initialValue));
        editBox.setResponder(value -> {
            try {
                int val = Integer.parseInt(value);
                if (val >= min && val <= max) {
                    onChange.accept(val);
                }
            } catch (NumberFormatException ignored) {}
        });
        editBox.setTooltip(Tooltip.create(Component.translatable(tooltipKey)));

        // 添加到可渲染列表以确保交互正常
        this.addRenderableWidget(editBox);

        configWidgets.add(new ConfigWidget(
            Component.translatable(labelKey),
            editBox,
            widgetIndex
        ));

        return y + ROW_HEIGHT;
    }

    /**
     * 添加字符串类型配置行
     */
    private int addStringConfigRow(int y, String labelKey, String tooltipKey, String initialValue, Consumer<String> onChange) {
        int widgetIndex = entryIndex++;

        EditBox editBox = new EditBox(this.minecraft.font, panelRight - PADDING - BUTTON_WIDTH, y, BUTTON_WIDTH, 20, Component.empty());
        editBox.setValue(initialValue);
        editBox.setResponder(onChange);
        editBox.setTooltip(Tooltip.create(Component.translatable(tooltipKey)));

        // 添加到可渲染列表以确保交互正常
        this.addRenderableWidget(editBox);

        configWidgets.add(new ConfigWidget(
            Component.translatable(labelKey),
            editBox,
            widgetIndex
        ));

        return y + ROW_HEIGHT;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 更新动画
        updateOpenAnimation();
        updateHoverEffects(mouseX, mouseY);
        updateParticles();
        updateTitleGlow();

        // 应用打开动画
        int animPanelTop = panelTop + (int) ((1.0f - openAnimationProgress) * 40);
        int animAlpha = (int) (openAnimationProgress * 255);
        float animScale = 0.9f + (openAnimationProgress * 0.1f);

        // 1. 渲染纯色背景（禁用模糊）
        guiGraphics.fill(0, 0, this.width, this.height, 0xFF1A1A2E);

        // 2. 渲染动态背景粒子
        renderParticles(guiGraphics);

        // 3. 渲染装饰性背景元素
        renderBackgroundDecoration(guiGraphics, animAlpha);

        // 4. 渲染面板背景（带动画）
        renderPanelBackground(guiGraphics, animPanelTop, animAlpha, animScale);

        // 5. 渲染标题（带动画和发光效果）
        renderTitle(guiGraphics, animAlpha);

        // 6. 启用裁剪区域
        guiGraphics.enableScissor(panelLeft + PADDING, panelTop + 50, panelRight - PADDING, panelBottom - 15);

        // 7. 渲染配置项
        renderConfigRows(guiGraphics, mouseX, mouseY, partialTick);

        // 8. 禁用裁剪
        guiGraphics.disableScissor();

        // 9. 渲染滚动条
        renderScrollbar(guiGraphics);

        // 10. 渲染固定控件（完成按钮）- 使用 super.render 确保交互正常
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    /**
     * 渲染标题带动画和发光效果
     */
    private void renderTitle(GuiGraphics guiGraphics, int alpha) {
        int titleY = panelTop - 28 + (int) ((1.0f - openAnimationProgress) * 20);

        // 发光效果
        int glowIntensity = (int) (titleGlow * 30);
        int titleColor = (Math.min(alpha, 255) << 24) | ((0xE0 + glowIntensity) << 16) | ((0xE0 + glowIntensity) << 8) | (0xFF);

        // 阴影效果
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2 + 1, titleY + 1, (Math.min(alpha, 100) << 24) | 0x000000);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, titleY, titleColor);
    }

    /**
     * 更新标题发光动画
     */
    private void updateTitleGlow() {
        if (titleGlowIncreasing) {
            titleGlow += 0.02f;
            if (titleGlow >= 1.0f) {
                titleGlow = 1.0f;
                titleGlowIncreasing = false;
            }
        } else {
            titleGlow -= 0.02f;
            if (titleGlow <= 0.0f) {
                titleGlow = 0.0f;
                titleGlowIncreasing = true;
            }
        }
    }

    /**
     * 渲染动态背景粒子
     */
    private void renderParticles(GuiGraphics guiGraphics) {
        for (Particle particle : particles) {
            int alpha = (int) (particle.life * 255);
            int color = (alpha << 24) | particle.color;
            int size = (int) (particle.size * particle.life);
            if (size > 0) {
                guiGraphics.fill(
                    (int) particle.x - size, (int) particle.y - size,
                    (int) particle.x + size, (int) particle.y + size,
                    color
                );
            }
        }
    }

    /**
     * 更新粒子系统
     */
    private void updateParticles() {
        long currentTime = System.currentTimeMillis();

        // 生成新粒子
        if (currentTime - lastParticleSpawn > 100 && particles.size() < 30) {
            particles.add(new Particle(
                random.nextFloat() * this.width,
                random.nextFloat() * this.height,
                (random.nextFloat() - 0.5f) * 0.5f,
                (random.nextFloat() - 0.5f) * 0.5f,
                0.5f + random.nextFloat() * 1.5f,
                1.0f + random.nextFloat() * 2.0f,
                random.nextBoolean() ? 0x4A90E2 : 0x9B59B6
            ));
            lastParticleSpawn = currentTime;
        }

        // 更新现有粒子
        particles.removeIf(particle -> {
            particle.x += particle.vx;
            particle.y += particle.vy;
            particle.life -= 0.005f;
            return particle.life <= 0;
        });
    }

    /**
     * 渲染背景装饰
     */
    private void renderBackgroundDecoration(GuiGraphics guiGraphics, int alpha) {
        int gridAlpha = Math.min(alpha / 8, 20);
        int gridColor = (gridAlpha << 24) | 0xFFFFFF;

        // 渲染 subtle 的网格图案
        int gridSize = 50;
        for (int x = 0; x < this.width; x += gridSize) {
            guiGraphics.fill(x, 0, x + 1, this.height, gridColor);
        }
        for (int y = 0; y < this.height; y += gridSize) {
            guiGraphics.fill(0, y, this.width, y + 1, gridColor);
        }

        // 渲染角落装饰
        renderCornerDecoration(guiGraphics, panelLeft - 10, panelTop - 10, 20, true, alpha);
        renderCornerDecoration(guiGraphics, panelRight + 10, panelTop - 10, 20, false, alpha);
        renderCornerDecoration(guiGraphics, panelLeft - 10, panelBottom + 10, 20, true, alpha);
        renderCornerDecoration(guiGraphics, panelRight + 10, panelBottom + 10, 20, false, alpha);
    }

    /**
     * 渲染角落装饰
     */
    private void renderCornerDecoration(GuiGraphics guiGraphics, int x, int y, int size, boolean left, int alpha) {
        int color = (Math.min(alpha, 150) << 24) | 0x4A90E2;
        if (left) {
            guiGraphics.fill(x, y, x + size, y + 2, color);
            guiGraphics.fill(x, y, x + 2, y + size, color);
        } else {
            guiGraphics.fill(x - size, y, x, y + 2, color);
            guiGraphics.fill(x - 2, y, x, y + size, color);
        }
    }

    /**
     * 渲染面板背景
     */
    private void renderPanelBackground(GuiGraphics guiGraphics, int top, int alpha, float scale) {
        int bgAlpha = Math.min(alpha, 240);
        int bgColor = (bgAlpha << 24) | 0x16213E;
        int borderColor = (Math.min(alpha, 200) << 24) | 0x4A90E2;
        int innerBorderColor = (Math.min(alpha, 100) << 24) | 0x0F3460;

        // 主面板
        guiGraphics.fill(panelLeft, top, panelRight, panelBottom, bgColor);

        // 外边框
        guiGraphics.fill(panelLeft, top, panelRight, top + 2, borderColor);
        guiGraphics.fill(panelLeft, panelBottom - 2, panelRight, panelBottom, borderColor);
        guiGraphics.fill(panelLeft, top, panelLeft + 2, panelBottom, borderColor);
        guiGraphics.fill(panelRight - 2, top, panelRight, panelBottom, borderColor);

        // 内边框
        guiGraphics.fill(panelLeft + 2, top + 2, panelRight - 2, top + 4, innerBorderColor);
        guiGraphics.fill(panelLeft + 2, panelBottom - 4, panelRight - 2, panelBottom - 2, innerBorderColor);
        guiGraphics.fill(panelLeft + 2, top + 2, panelLeft + 4, panelBottom - 2, innerBorderColor);
        guiGraphics.fill(panelRight - 4, top + 2, panelRight - 2, panelBottom - 2, innerBorderColor);

        // 顶部高光
        guiGraphics.fill(panelLeft + 4, top + 4, panelRight - 4, top + 5, 0x20FFFFFF);

        // 底部阴影
        guiGraphics.fill(panelLeft + 4, panelBottom - 5, panelRight - 4, panelBottom - 4, 0x10000000);
    }

    /**
     * 渲染配置行 - 使用多种动画效果
     */
    private void renderConfigRows(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int currentY = panelTop + 55 - scrollOffset;

        for (ConfigWidget widget : configWidgets) {
            float entryProgress = getEntryAnimationProgress(widget.index);
            float hover = hoverProgress[widget.index];

            if (entryProgress <= 0.01f) continue;

            // 根据动画类型计算偏移
            int animOffsetX = 0;
            int animOffsetY = 0;
            float animScale = 1.0f;
            float animRotation = 0.0f;

            EntryAnimationType animType = entryAnimationTypes[widget.index % entryAnimationTypes.length];

            switch (animType) {
                case SLIDE_FROM_RIGHT:
                    animOffsetX = (int) ((1.0f - entryProgress) * 80);
                    break;
                case SLIDE_FROM_LEFT:
                    animOffsetX = (int) ((1.0f - entryProgress) * -80);
                    break;
                case SLIDE_FROM_BOTTOM:
                    animOffsetY = (int) ((1.0f - entryProgress) * 40);
                    break;
                case SCALE_UP:
                    animScale = 0.7f + (entryProgress * 0.3f);
                    break;
                case FADE_IN:
                    // 只有透明度变化，无位移
                    break;
                case BOUNCE:
                    animOffsetY = (int) ((1.0f - easeOutBounce(entryProgress)) * -30);
                    break;
                case FLIP:
                    animScale = (float) Math.cos((1.0f - entryProgress) * Math.PI / 2);
                    break;
            }

            int drawY = currentY + animOffsetY;
            int drawX = panelLeft + PADDING + animOffsetX;

            // 检查是否在可视区域内
            boolean visible = drawY + ROW_HEIGHT > panelTop + 50 && drawY < panelBottom - 15;

            if (visible) {
                int animAlpha = (int) (entryProgress * 255);

                // 悬停效果 - 背景高亮
                if (hover > 0.01f) {
                    int hoverColor = ((int) (hover * 30) << 24) | 0x4A90E2;
                    guiGraphics.fill(
                        panelLeft + PADDING - 5, drawY - 2,
                        panelRight - PADDING + 5, drawY + ROW_HEIGHT - 4,
                        hoverColor
                    );
                }

                // 渲染标签
                int labelColor = (animAlpha << 24) | 0xE0E0E0;
                guiGraphics.drawString(this.font, widget.label, drawX, drawY + 8, labelColor);

                // 更新控件位置
                int widgetX = panelRight - PADDING - BUTTON_WIDTH + animOffsetX;
                widget.widget.setX(widgetX);
                widget.widget.setY(drawY + 4);
                widget.widget.setAlpha(animAlpha / 255.0f);
                widget.widget.visible = true;

                // 渲染控件
                widget.widget.render(guiGraphics, mouseX, mouseY, partialTick);
            } else {
                widget.widget.visible = false;
            }

            currentY += ROW_HEIGHT;
        }
    }

    /**
     * 渲染滚动条
     */
    private void renderScrollbar(GuiGraphics guiGraphics) {
        int maxScroll = Math.max(0, totalContentHeight - (panelHeight - 70));
        if (maxScroll <= 0) return;

        int scrollbarHeight = panelHeight - 70;
        int thumbHeight = Math.max(30, (scrollbarHeight * scrollbarHeight) / (scrollbarHeight + maxScroll));
        int thumbY = panelTop + 55 + (int) ((scrollbarHeight - thumbHeight) * ((float) scrollOffset / maxScroll));

        // 滚动条背景
        guiGraphics.fill(panelRight - 8, panelTop + 55, panelRight - 4, panelBottom - 15, 0x30FFFFFF);

        // 滚动条滑块
        int thumbColor = isScrolling ? 0xFF4A90E2 : 0xFF6B7B8F;
        guiGraphics.fill(panelRight - 8, thumbY, panelRight - 4, thumbY + thumbHeight, thumbColor);
    }

    /**
     * 更新悬停效果
     */
    private void updateHoverEffects(int mouseX, int mouseY) {
        int currentY = panelTop + 55 - scrollOffset;
        int newHoveredIndex = -1;

        for (ConfigWidget widget : configWidgets) {
            float entryProgress = getEntryAnimationProgress(widget.index);
            if (entryProgress < 1.0f) {
                hoverProgress[widget.index] = Math.max(0, hoverProgress[widget.index] - 0.1f);
                currentY += ROW_HEIGHT;
                continue;
            }

            int drawY = currentY;
            int drawX = panelLeft + PADDING;
            int widgetX = panelRight - PADDING - BUTTON_WIDTH;

            // 检查鼠标是否悬停在此行
            boolean isHovered = mouseX >= drawX && mouseX <= widgetX + BUTTON_WIDTH &&
                               mouseY >= drawY && mouseY <= drawY + ROW_HEIGHT &&
                               mouseY > panelTop + 50 && mouseY < panelBottom - 15;

            if (isHovered) {
                newHoveredIndex = widget.index;
                hoverProgress[widget.index] = Math.min(1.0f, hoverProgress[widget.index] + 0.15f);
            } else {
                hoverProgress[widget.index] = Math.max(0.0f, hoverProgress[widget.index] - 0.1f);
            }

            currentY += ROW_HEIGHT;
        }

        hoveredEntryIndex = newHoveredIndex;
    }

    /**
     * 获取配置项进入动画进度
     */
    private float getEntryAnimationProgress(int index) {
        if (!firstOpen) return 1.0f;

        long elapsed = System.currentTimeMillis() - animationStartTime;
        float delay = index * 80f;  // 每项延迟80ms
        float duration = 400f;      // 动画持续时间400ms
        float progress = Math.max(0.0f, Math.min(1.0f, (elapsed - delay) / duration));

        return easeOutCubic(progress);
    }

    /**
     * 更新打开动画
     */
    private void updateOpenAnimation() {
        if (firstOpen) {
            long elapsed = System.currentTimeMillis() - animationStartTime;
            openAnimationProgress = Math.min(1.0f, elapsed / 600.0f);
            openAnimationProgress = easeOutCubic(openAnimationProgress);

            if (openAnimationProgress >= 1.0f) {
                firstOpen = false;
            }
        }
    }

    /**
     * 缓动函数 easeOutCubic
     */
    private float easeOutCubic(float t) {
        return 1.0f - (float) Math.pow(1.0f - t, 3);
    }

    /**
     * 缓动函数 easeOutBounce
     */
    private float easeOutBounce(float t) {
        if (t < 1 / 2.75f) {
            return 7.5625f * t * t;
        } else if (t < 2 / 2.75f) {
            t -= 1.5f / 2.75f;
            return 7.5625f * t * t + 0.75f;
        } else if (t < 2.5f / 2.75f) {
            t -= 2.25f / 2.75f;
            return 7.5625f * t * t + 0.9375f;
        } else {
            t -= 2.625f / 2.75f;
            return 7.5625f * t * t + 0.984375f;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int maxScroll = Math.max(0, totalContentHeight - (panelHeight - 70));
        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - scrollY * 20));
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 检查是否点击了滚动条
        int maxScroll = Math.max(0, totalContentHeight - (panelHeight - 70));
        if (maxScroll > 0) {
            int scrollbarHeight = panelHeight - 70;
            int thumbHeight = Math.max(30, (scrollbarHeight * scrollbarHeight) / (scrollbarHeight + maxScroll));
            int thumbY = panelTop + 55 + (int) ((scrollbarHeight - thumbHeight) * ((float) scrollOffset / maxScroll));

            if (mouseX >= panelRight - 8 && mouseX <= panelRight - 4 &&
                mouseY >= thumbY && mouseY <= thumbY + thumbHeight) {
                isScrolling = true;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isScrolling = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isScrolling) {
            int maxScroll = Math.max(0, totalContentHeight - (panelHeight - 70));
            if (maxScroll > 0) {
                int scrollbarHeight = panelHeight - 70;
                int thumbHeight = Math.max(30, (scrollbarHeight * scrollbarHeight) / (scrollbarHeight + maxScroll));
                int availableHeight = scrollbarHeight - thumbHeight;

                if (availableHeight > 0) {
                    scrollOffset = (int) Math.max(0, Math.min(maxScroll,
                        ((int) mouseY - (panelTop + 55) - thumbHeight / 2) * maxScroll / availableHeight));
                }
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
        // 空实现 - 禁用模糊背景，避免文字模糊
    }

    @Override
    public void onClose() {
        // 保存配置
        Config.SPEC.save();
        // 返回上级界面
        this.minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * 粒子类 - 用于背景动画
     */
    private static class Particle {
        float x, y;
        float vx, vy;
        float life;
        float size;
        int color;

        Particle(float x, float y, float vx, float vy, float life, float size, int color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.life = life;
            this.size = size;
            this.color = color;
        }
    }

    /**
     * 配置控件包装类
     */
    private static class ConfigWidget {
        final Component label;
        final AbstractWidget widget;
        final int index;

        ConfigWidget(Component label, AbstractWidget widget, int index) {
            this.label = label;
            this.widget = widget;
            this.index = index;
        }
    }
}
