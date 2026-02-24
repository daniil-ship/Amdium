package com.amdium.core;

import com.amdium.Amdium;
import com.amdium.config.AmdiumConfig;
import com.amdium.optimization.*;

import java.util.ArrayList;
import java.util.List;

public class AMDOptimizer {

    private final GPUInfo gpuInfo;
    private final CPUInfo cpuInfo;
    private final boolean isAPU;
    private final List<IOptimization> optimizations = new ArrayList<>();
    private int tickCounter = 0;

    public AMDOptimizer(GPUInfo gpuInfo, CPUInfo cpuInfo, boolean isAPU) {
        this.gpuInfo = gpuInfo;
        this.cpuInfo = cpuInfo;
        this.isAPU = isAPU;

        initOptimizations();
    }

    private void initOptimizations() {
        if (AmdiumConfig.CHUNK_OPTIMIZATION.get()) {
            optimizations.add(new ChunkRenderOptimizer(gpuInfo, cpuInfo));
        }
        if (AmdiumConfig.SHADER_OPTIMIZATION.get()) {
            optimizations.add(new ShaderOptimizer(gpuInfo));
        }
        if (AmdiumConfig.MEMORY_OPTIMIZATION.get()) {
            optimizations.add(new MemoryOptimizer(gpuInfo, cpuInfo, isAPU));
        }
        if (AmdiumConfig.THREAD_OPTIMIZATION.get()) {
            optimizations.add(new ThreadOptimizer(cpuInfo));
        }
        if (AmdiumConfig.PIPELINE_OPTIMIZATION.get()) {
            optimizations.add(new RenderPipelineOptimizer(gpuInfo));
        }
        if (AmdiumConfig.APU_OPTIMIZATION.get() && isAPU) {
            optimizations.add(new APUOptimizer(gpuInfo, cpuInfo));
        }
    }

    public void applyOptimizations() {
        if (!AmdiumConfig.ENABLED.get()) {
            Amdium.LOGGER.info("Amdium optimizations are disabled in config");
            return;
        }

        Amdium.LOGGER.info("Applying {} optimizations...", optimizations.size());

        for (IOptimization opt : optimizations) {
            try {
                opt.apply();
                Amdium.LOGGER.info("  ✓ {} applied successfully", opt.getName());
            } catch (Exception e) {
                Amdium.LOGGER.error("  ✗ {} failed: {}", opt.getName(), e.getMessage());
            }
        }

        // Применяем системные свойства JVM для AMD
        applyJVMOptimizations();

        Amdium.LOGGER.info("All optimizations applied!");
    }

    /**
     * Вызывается каждый тик для динамических оптимизаций
     */
    public void tick() {
        tickCounter++;

        // Каждые 20 тиков (1 секунда)
        if (tickCounter % 20 == 0) {
            for (IOptimization opt : optimizations) {
                try {
                    opt.tick();
                } catch (Exception e) {
                    // Тихо пропускаем ошибки в тиках
                }
            }
        }

        // Каждые 600 тиков (30 секунд) — тяжёлые оптимизации
        if (tickCounter % 600 == 0) {
            for (IOptimization opt : optimizations) {
                try {
                    opt.periodicUpdate();
                } catch (Exception e) {
                    Amdium.LOGGER.debug("Periodic update failed for {}: {}",
                            opt.getName(), e.getMessage());
                }
            }
            tickCounter = 0;
        }
    }

    private void applyJVMOptimizations() {
        if (!cpuInfo.isAMD()) return;

        Amdium.LOGGER.info("Applying JVM hints for AMD CPU...");

        // Подсказки для JVM оптимизации на AMD
        try {
            // Оптимальный размер кеш-линии для AMD Zen
            if (cpuInfo.isZen()) {
                System.setProperty("sun.misc.Contended.padding", "64");
            }
        } catch (SecurityException e) {
            Amdium.LOGGER.debug("Cannot set JVM property: {}", e.getMessage());
        }
    }

    public List<IOptimization> getOptimizations() {
        return optimizations;
    }

    public String getStatusReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("Amdium Status Report:\n");
        sb.append("GPU: ").append(gpuInfo.getName()).append("\n");
        sb.append("CPU: ").append(cpuInfo.getName()).append("\n");
        sb.append("APU: ").append(isAPU).append("\n");
        sb.append("Active optimizations:\n");
        for (IOptimization opt : optimizations) {
            sb.append("  - ").append(opt.getName())
                    .append(": ").append(opt.isActive() ? "Active" : "Inactive")
                    .append("\n");
        }
        return sb.toString();
    }
}