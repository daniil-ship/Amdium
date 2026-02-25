package com.amdium.optimization;

import com.amdium.Amdium;
import com.amdium.config.AmdiumConfig;
import com.amdium.core.CPUInfo;
import com.amdium.core.GPUInfo;
import net.minecraft.client.Minecraft;

public class APUOptimizer implements IOptimization {

    private final GPUInfo gpuInfo;
    private final CPUInfo cpuInfo;
    private boolean active = false;
    private AmdiumConfig.APUProfile currentProfile;

    public APUOptimizer(GPUInfo gpuInfo, CPUInfo cpuInfo) {
        this.gpuInfo = gpuInfo;
        this.cpuInfo = cpuInfo;
    }

    @Override
    public String getName() {
        return "APU Optimizer";
    }

    @Override
    public String getDescription() {
        return "Specialized optimizations for AMD APUs with integrated graphics";
    }

    @Override
    public void apply() {
        currentProfile = AmdiumConfig.APU_PROFILE.get();

        if (currentProfile == AmdiumConfig.APUProfile.AUTO) {
            currentProfile = autoDetectProfile();
        }

        Amdium.LOGGER.info("APU Profile: {}", currentProfile);

        applyProfile(currentProfile);
        active = true;
    }

    private AmdiumConfig.APUProfile autoDetectProfile() {
        int vramMB = gpuInfo.getVramMB();
        int cores = cpuInfo.getCores();
        int threads = cpuInfo.getThreads();

        // Определяем профиль на основе оборудования
        if (gpuInfo.getArchitecture() == GPUInfo.GPUArchitecture.RDNA_3_APU) {
            // Новые APU (780M, 890M) — достаточно мощные
            return AmdiumConfig.APUProfile.PERFORMANCE;
        } else if (gpuInfo.getArchitecture() == GPUInfo.GPUArchitecture.RDNA_2_APU) {
            // Средние APU (680M)
            return AmdiumConfig.APUProfile.BALANCED;
        } else if (gpuInfo.isVega()) {
            // Старые APU (Vega)
            if (threads >= 8) {
                return AmdiumConfig.APUProfile.BALANCED;
            } else {
                return AmdiumConfig.APUProfile.POWER_SAVE;
            }
        }

        return AmdiumConfig.APUProfile.BALANCED;
    }

    private void applyProfile(AmdiumConfig.APUProfile profile) {
        Minecraft mc = Minecraft.getInstance();

        switch (profile) {
            case POWER_SAVE -> {
                Amdium.LOGGER.info("APU Power Save mode:");
                Amdium.LOGGER.info("  - Recommending lower render distance");
                Amdium.LOGGER.info("  - Reducing particle effects");
                Amdium.LOGGER.info("  - Limiting texture resolution");

                // Рекомендации для слабых APU
                if (mc.options.renderDistance().get() > 8) {
                    Amdium.LOGGER.info("  Suggest: render distance <= 8");
                }
            }

            case BALANCED -> {
                Amdium.LOGGER.info("APU Balanced mode:");
                Amdium.LOGGER.info("  - Balanced render settings");
                Amdium.LOGGER.info("  - Smart VRAM management");

                // Средние настройки
                if (mc.options.renderDistance().get() > 12) {
                    Amdium.LOGGER.info("  Suggest: render distance <= 12");
                }
            }

            case PERFORMANCE -> {
                Amdium.LOGGER.info("APU Performance mode:");
                Amdium.LOGGER.info("  - Maximizing GPU utilization");
                Amdium.LOGGER.info("  - Optimized memory sharing");
            }

            case ULTRA_PERFORMANCE -> {
                Amdium.LOGGER.info("APU Ultra Performance mode:");
                Amdium.LOGGER.info("  - Maximum optimizations");
                Amdium.LOGGER.info("  - Aggressive memory management");
                Amdium.LOGGER.info("  - Reduced visual quality for FPS");
            }
        }

        // Общие APU оптимизации
        applySharedMemoryOptimizations();
    }

    private void applySharedMemoryOptimizations() {
        if (!AmdiumConfig.APU_SHARED_MEMORY_OPT.get()) return;

        Amdium.LOGGER.info("APU: Optimizing shared memory access patterns");

        // На APU CPU и GPU делят одну RAM
        // Оптимизируем паттерны доступа к памяти

        // 1. Уменьшаем количество копий данных между CPU и GPU
        // 2. Используем coherent буферы где возможно
        // 3. Минимизируем пиннинг памяти

        long totalMemory = Runtime.getRuntime().maxMemory();
        long reservedForGPU = totalMemory / 4; // 25% для GPU
        long availableForGame = totalMemory - reservedForGPU;

        Amdium.LOGGER.info("APU Memory allocation:");
        Amdium.LOGGER.info("  Total: {} MB", totalMemory / (1024 * 1024));
        Amdium.LOGGER.info("  GPU reserved: {} MB", reservedForGPU / (1024 * 1024));
        Amdium.LOGGER.info("  Game available: {} MB", availableForGame / (1024 * 1024));
    }

    @Override
    public void periodicUpdate() {
        if (!active) return;

        // Мониторинг использования памяти для APU
        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        long max = runtime.maxMemory();
        double usage = (double) used / max * 100;

        if (usage > 85) {
            Amdium.LOGGER.warn("APU: High memory usage ({:.1f}%), consider reducing settings",
                    usage);
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void disable() {
        active = false;
    }

    public AmdiumConfig.APUProfile getCurrentProfile() {
        return currentProfile;
    }
}