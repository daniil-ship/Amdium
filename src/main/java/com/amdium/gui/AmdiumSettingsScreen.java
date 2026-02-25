package com.amdium.gui;

import com.amdium.Amdium;
import com.amdium.config.AmdiumConfig;
import com.amdium.util.BenchmarkUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class AmdiumSettingsScreen extends Screen {

    private final Screen parent;
    private int scrollOffset = 0;

    public AmdiumSettingsScreen(Screen parent) {
        super(Component.translatable("amdium.settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = 40;
        int buttonWidth = 200;
        int buttonHeight = 20;
        int spacing = 24;

        y = 80;

        // Toggle buttons
        addToggleButton(centerX - buttonWidth - 5, y, buttonWidth, buttonHeight,
                "amdium.option.chunk_optimization", AmdiumConfig.CHUNK_OPTIMIZATION);
        addToggleButton(centerX + 5, y, buttonWidth, buttonHeight,
                "amdium.option.shader_optimization", AmdiumConfig.SHADER_OPTIMIZATION);
        y += spacing;

        addToggleButton(centerX - buttonWidth - 5, y, buttonWidth, buttonHeight,
                "amdium.option.memory_optimization", AmdiumConfig.MEMORY_OPTIMIZATION);
        addToggleButton(centerX + 5, y, buttonWidth, buttonHeight,
                "amdium.option.thread_optimization", AmdiumConfig.THREAD_OPTIMIZATION);
        y += spacing;

        addToggleButton(centerX - buttonWidth - 5, y, buttonWidth, buttonHeight,
                "amdium.option.pipeline_optimization", AmdiumConfig.PIPELINE_OPTIMIZATION);
        addToggleButton(centerX + 5, y, buttonWidth, buttonHeight,
                "amdium.option.apu_mode", AmdiumConfig.APU_OPTIMIZATION);
        y += spacing;

        addToggleButton(centerX - buttonWidth - 5, y, buttonWidth, buttonHeight,
                "amdium.option.show_hud", AmdiumConfig.SHOW_HUD);
        addToggleButton(centerX + 5, y, buttonWidth, buttonHeight,
                "amdium.option.aggressive_mode", AmdiumConfig.AGGRESSIVE_MODE);
        y += spacing;

        addToggleButton(centerX - buttonWidth - 5, y, buttonWidth, buttonHeight,
                "amdium.option.vram_management", AmdiumConfig.SMART_VRAM);
        y += spacing;

        // Benchmark button
        y += 10;
        addRenderableWidget(Button.builder(
                        Component.literal("Run Benchmark (10s)"),
                        btn -> {
                            BenchmarkUtil.startBenchmark(10);
                            onClose();
                        })
                .pos(centerX - 100, y)
                .size(200, 20)
                .tooltip(Tooltip.create(Component.literal("Run a 10-second FPS benchmark")))
                .build());

        y += spacing;

        // Done button
        addRenderableWidget(Button.builder(
                        CommonComponents.GUI_DONE,
                        btn -> onClose())
                .pos(centerX - 100, this.height - 30)
                .size(200, 20)
                .build());
    }

    private void addToggleButton(int x, int y, int width, int height,
                                 String translationKey,
                                 net.minecraftforge.common.ForgeConfigSpec.BooleanValue config) {
        boolean value = config.get();
        String status = value ? "§aON" : "§cOFF";

        Button button = Button.builder(
                        Component.translatable(translationKey).append(": " + status),
                        btn -> {
                            config.set(!config.get());
                            rebuildWidgets();
                        })
                .pos(x, y)
                .size(width, height)
                .build();

        addRenderableWidget(button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        // Title
        graphics.drawCenteredString(this.font,
                Component.literal("§c§lAmdium §r§7- AMD Optimizer v" + Amdium.VERSION),
                this.width / 2, 10, 0xFFFFFFFF);

        // Hardware info
        Amdium amdium = Amdium.getInstance();
        int infoY = 25;

        if (amdium.getGpuInfo() != null) {
            String gpuText = "GPU: " + amdium.getGpuInfo().getName();
            if (amdium.getGpuInfo().isAMD()) {
                gpuText += " §a(AMD ✓)";
            }
            graphics.drawCenteredString(this.font, gpuText, this.width / 2, infoY, 0xFFCCCCCC);
            infoY += 10;
        }

        if (amdium.getCpuInfo() != null) {
            String cpuText = "CPU: " + amdium.getCpuInfo().getName();
            if (amdium.getCpuInfo().isAMD()) {
                cpuText += " §a(AMD ✓)";
            }
            graphics.drawCenteredString(this.font, cpuText, this.width / 2, infoY, 0xFFCCCCCC);
            infoY += 10;
        }

        if (amdium.isApuDetected()) {
            graphics.drawCenteredString(this.font, "§eAPU Mode Active",
                    this.width / 2, infoY, 0xFFFFFF00);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    protected void rebuildWidgets() {
        this.clearWidgets();
        this.init();
    }
}
