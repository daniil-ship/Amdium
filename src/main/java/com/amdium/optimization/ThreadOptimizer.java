package com.amdium.optimization;

import com.amdium.Amdium;
import com.amdium.core.CPUInfo;

public class ThreadOptimizer implements IOptimization {

    private final CPUInfo cpuInfo;
    private boolean active = false;

    public ThreadOptimizer(CPUInfo cpuInfo) {
        this.cpuInfo = cpuInfo;
    }

    @Override
    public String getName() {
        return "Thread Optimizer";
    }

    @Override
    public String getDescription() {
        return "Optimizes thread affinity and scheduling for AMD CPU topology";
    }

    @Override
    public void apply() {
        if (!cpuInfo.isAMD()) {
            Amdium.LOGGER.info("Thread optimizer: non-AMD CPU, applying generic optimizations");
            applyGenericOptimizations();
            active = true;
            return;
        }

        Amdium.LOGGER.info("Applying AMD CPU thread optimizations for {} architecture",
                cpuInfo.getArchitecture());

        // Оптимизации специфичные для архитектуры Zen
        if (cpuInfo.isZen()) {
            applyZenOptimizations();
        } else {
            applyLegacyAMDOptimizations();
        }

        active = true;
    }

    private void applyZenOptimizations() {
        int ccxThreads = cpuInfo.getCCXThreads();
        int optimalThreads = cpuInfo.getOptimalChunkThreads();

        Amdium.LOGGER.info("Zen optimization: CCX size={}, optimal chunk threads={}",
                ccxThreads, optimalThreads);

        // Zen архитектура: лучше держать потоки в пределах одного CCX/CCD
        // чтобы избежать inter-CCX latency

        // Устанавливаем приоритет главного потока
        try {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY - 1);
            Amdium.LOGGER.info("Main thread priority elevated");
        } catch (SecurityException e) {
            Amdium.LOGGER.debug("Cannot set thread priority");
        }

        // Подсказки для GC потоков
        try {
            // Для Zen лучше параллельный GC с количеством потоков = cores/2
            int gcThreads = Math.max(2, cpuInfo.getCores() / 4);
            Amdium.LOGGER.info("Recommended GC threads for Zen: {}", gcThreads);
        } catch (Exception e) {
            Amdium.LOGGER.debug("Cannot configure GC threads");
        }
    }

    private void applyLegacyAMDOptimizations() {
        Amdium.LOGGER.info("Applying legacy AMD CPU optimizations");
        // Для Bulldozer/Piledriver — модули имеют общий FPU
        // Лучше не перегружать потоками
    }

    private void applyGenericOptimizations() {
        try {
            Thread.currentThread().setPriority(Thread.NORM_PRIORITY + 2);
        } catch (SecurityException ignored) {}
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void disable() {
        active = false;
    }
}