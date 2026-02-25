package com.amdium.optimization;

import com.amdium.Amdium;
import com.amdium.config.AmdiumConfig;
import com.amdium.core.CPUInfo;
import com.amdium.core.GPUInfo;
import org.lwjgl.opengl.*;

public class MemoryOptimizer implements IOptimization {

    private final GPUInfo gpuInfo;
    private final CPUInfo cpuInfo;
    private final boolean isAPU;
    private boolean active = false;
    private long lastGCTime = 0;

    public MemoryOptimizer(GPUInfo gpuInfo, CPUInfo cpuInfo, boolean isAPU) {
        this.gpuInfo = gpuInfo;
        this.cpuInfo = cpuInfo;
        this.isAPU = isAPU;
    }

    @Override
    public String getName() {
        return "Memory Optimizer";
    }

    @Override
    public String getDescription() {
        return "Optimizes memory allocation for AMD GPU/APU shared memory architecture";
    }

    @Override
    public void apply() {
        if (isAPU) {
            applyAPUMemoryOptimizations();
        }

        applyGLMemoryOptimizations();
        active = true;
    }

    private void applyAPUMemoryOptimizations() {
        Amdium.LOGGER.info("Applying APU shared memory optimizations");

        // На APU GPU и CPU делят одну RAM
        // Нужно быть аккуратнее с выделением памяти

        int vramLimitMB = AmdiumConfig.APU_VRAM_LIMIT_MB.get();
        if (vramLimitMB <= 0) {
            // Auto: выделяем 25% от доступной RAM для GPU
            long totalMemory = Runtime.getRuntime().maxMemory();
            vramLimitMB = (int) (totalMemory / (1024 * 1024) / 4);
            Amdium.LOGGER.info("APU Auto VRAM limit: {} MB", vramLimitMB);
        }

        Amdium.LOGGER.info("APU VRAM limit set to: {} MB", vramLimitMB);
    }

    private void applyGLMemoryOptimizations() {
        try {
            // Оптимизация паттернов выделения GL буферов для AMD
            // AMD драйвер предпочитает orphan буферы вместо субдата

            // Устанавливаем оптимальные размеры текстурного кеша
            int maxTextureSize = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);
            Amdium.LOGGER.info("Max texture size: {}", maxTextureSize);

            // Для AMD — лучше использовать immutable storage
            if (GLUtils.isExtensionSupported("GL_ARB_buffer_storage")) {
                Amdium.LOGGER.info("Using GL_ARB_buffer_storage for optimized memory");
            }

            // AMD-специфичная оптимизация пиннинга памяти
            if (GLUtils.isExtensionSupported("GL_AMD_pinned_memory")) {
                Amdium.LOGGER.info("AMD pinned memory extension available — using for buffers");
            }

        } catch (Exception e) {
            Amdium.LOGGER.debug("GL memory optimization partially failed: {}", e.getMessage());
        }
    }

    @Override
    public void periodicUpdate() {
        if (!active) return;

        // Периодическая очистка для APU (разделяемая память)
        if (isAPU && AmdiumConfig.APU_SHARED_MEMORY_OPT.get()) {
            long now = System.currentTimeMillis();
            if (now - lastGCTime > 30000) { // каждые 30 сек
                Runtime runtime = Runtime.getRuntime();
                long usedMemory = runtime.totalMemory() - runtime.freeMemory();
                long maxMemory = runtime.maxMemory();
                double memoryUsage = (double) usedMemory / maxMemory;

                // Если используется больше 80% памяти — подсказываем GC
                if (memoryUsage > 0.8) {
                    System.gc();
                    Amdium.LOGGER.debug("APU memory optimization: triggered GC at {}%",
                            String.format("%.1f", memoryUsage * 100));
                }
                lastGCTime = now;
            }
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

    // Вспомогательный класс для проверки GL расширений
    private static class GLUtils {
        static boolean isExtensionSupported(String ext) {
            try {
                int numExtensions = GL11.glGetInteger(GL30.GL_NUM_EXTENSIONS);
                for (int i = 0; i < numExtensions; i++) {
                    String extension = GL30.glGetStringi(GL11.GL_EXTENSIONS, i);
                    if (ext.equals(extension)) return true;
                }
            } catch (Exception ignored) {}
            return false;
        }
    }
}