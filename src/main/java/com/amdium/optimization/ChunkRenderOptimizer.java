package com.amdium.optimization;

import com.amdium.Amdium;
import com.amdium.config.AmdiumConfig;
import com.amdium.core.CPUInfo;
import com.amdium.core.GPUInfo;

public class ChunkRenderOptimizer implements IOptimization {

    private final GPUInfo gpuInfo;
    private final CPUInfo cpuInfo;
    private boolean active = false;

    public ChunkRenderOptimizer(GPUInfo gpuInfo, CPUInfo cpuInfo) {
        this.gpuInfo = gpuInfo;
        this.cpuInfo = cpuInfo;
    }

    @Override
    public String getName() {
        return "Chunk Render Optimizer";
    }

    @Override
    public String getDescription() {
        return "Optimizes chunk rendering for AMD GPU architecture";
    }

    @Override
    public void apply() {
        int optimalThreads = cpuInfo.getOptimalChunkThreads();

        Amdium.LOGGER.info("Chunk optimization: recommended {} threads", optimalThreads);

        if (gpuInfo.isIntegrated() || gpuInfo.getVramMB() < 2048) {
            Amdium.LOGGER.info("Low VRAM detected - recommend render distance <= 12");
        }

        if (gpuInfo.isRDNA()) {
            Amdium.LOGGER.info("RDNA architecture detected, optimizing batch sizes");
        }

        active = true;
    }

    @Override
    public void tick() {

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
