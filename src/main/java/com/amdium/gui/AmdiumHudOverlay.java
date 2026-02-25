package com.amdium.gui;

import com.amdium.Amdium;
import com.amdium.config.AmdiumConfig;
import com.amdium.core.GPUInfo;
import com.amdium.core.CPUInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class AmdiumHudOverlay implements IGuiOverlay {

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        if (!AmdiumConfig.SHOW_HUD.get()) return;
        if (!AmdiumConfig.ENABLED.get()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.renderDebug) return; // F3

        Amdium amdium = Amdium.getInstance();
        if (amdium == null || amdium.getGpuInfo() == null) return;

        Font font = mc.font;
        int x = 5;
        int y = 5;
        int lineHeight = 10;
        int color = 0xFFFFFFFF;
        int accentColor = 0xFFED1C24; // AMD Red
        int greenColor = 0xFF00FF00;
        int yellowColor = 0xFFFFFF00;

        graphics.drawString(font, "§c[Amdium] §rv" + Amdium.VERSION, x, y, accentColor, true);
        y += lineHeight;

        // FPS
        int fps = mc.getFps();
        int fpsColor = fps >= 60 ? greenColor : (fps >= 30 ? yellowColor : 0xFFFF0000);
        graphics.drawString(font, "FPS: " + fps, x, y, fpsColor, true);
        y += lineHeight;

        // GPU info
        GPUInfo gpu = amdium.getGpuInfo();
        if (gpu != null) {
            String gpuName = gpu.getName();
            if (gpuName.length() > 35) gpuName = gpuName.substring(0, 35) + "...";
            graphics.drawString(font, "GPU: " + gpuName, x, y, color, true);
            y += lineHeight;

            if (gpu.isAMD()) {
                graphics.drawString(font, "Arch: " + gpu.getArchitecture().name(), x, y, color, true);
                y += lineHeight;

                graphics.drawString(font, "VRAM: " + gpu.getVramMB() + " MB", x, y, color, true);
                y += lineHeight;
            }
        }

        // APU indicator
        if (amdium.isApuDetected()) {
            graphics.drawString(font, "§e[APU Mode]", x, y, yellowColor, true);
            y += lineHeight;
        }

        // Memory usage
        Runtime runtime = Runtime.getRuntime();
        long usedMB = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long maxMB = runtime.maxMemory() / (1024 * 1024);
        double memPercent = (double) usedMB / maxMB * 100;
        int memColor = memPercent < 70 ? greenColor : (memPercent < 85 ? yellowColor : 0xFFFF0000);
        graphics.drawString(font, String.format("RAM: %d/%d MB (%.0f%%)", usedMB, maxMB, memPercent),
                x, y, memColor, true);
        y += lineHeight;

        // Optimization status
        if (amdium.isOptimizationsApplied()) {
            graphics.drawString(font, "§a✓ Optimized", x, y, greenColor, true);
        } else {
            graphics.drawString(font, "§c✗ Not optimized", x, y, 0xFFFF0000, true);
        }

        // Benchmark progress
        if (com.amdium.util.BenchmarkUtil.isRunning()) {
            y += lineHeight;
            int progress = com.amdium.util.BenchmarkUtil.getProgress();
            graphics.drawString(font, "§6Benchmarking: " + progress + "%", x, y, yellowColor, true);
        }
    }

}
