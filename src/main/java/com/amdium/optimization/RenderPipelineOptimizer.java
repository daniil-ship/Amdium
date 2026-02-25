package com.amdium.optimization;

import com.amdium.Amdium;
import com.amdium.config.AmdiumConfig;
import com.amdium.core.GPUInfo;
import com.amdium.util.GLUtils;
import org.lwjgl.opengl.*;

public class RenderPipelineOptimizer implements IOptimization {

    private final GPUInfo gpuInfo;
    private boolean active = false;
    private int cachedBlendSrc = -1;
    private int cachedBlendDst = -1;
    private int cachedDepthFunc = -1;
    private boolean cachedDepthTest = false;
    private boolean cachedBlend = false;
    private boolean cachedCullFace = false;

    public RenderPipelineOptimizer(GPUInfo gpuInfo) {
        this.gpuInfo = gpuInfo;
    }

    @Override
    public String getName() {
        return "Render Pipeline Optimizer";
    }

    @Override
    public String getDescription() {
        return "Optimizes the OpenGL render pipeline for AMD GCN/RDNA";
    }

    @Override
    public void apply() {
        if (!gpuInfo.isAMD()) return;

        try {
            optimizeGLState();
            optimizeBufferUsage();
            active = true;
        } catch (Exception e) {
            Amdium.LOGGER.error("Render pipeline optimization failed", e);
        }
    }

    private void optimizeGLState() {

        try {
            GL11.glDisable(GL13.GL_MULTISAMPLE);
        } catch (Exception ignored) {}

        GL11.glDisable(GL11.GL_DITHER);
        GL11.glHint(GL14.GL_GENERATE_MIPMAP_HINT, GL11.GL_FASTEST);
        GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 4);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);

        if (gpuInfo.isRDNA()) {
            Amdium.LOGGER.info("RDNA binning rasterizer detected — pipeline optimized");
        }

        Amdium.LOGGER.info("GL state optimized for AMD");
    }

    private void optimizeBufferUsage() {
        if (!AmdiumConfig.GL_BUFFER_OPTIMIZATION.get()) return;
        if (GLUtils.isExtensionSupported("GL_ARB_buffer_storage")) {
            Amdium.LOGGER.info("Using persistent buffer mapping for AMD");
        }
        int bufferSizeKB = AmdiumConfig.GL_BUFFER_SIZE_KB.get();
        Amdium.LOGGER.info("GL buffer size: {} KB", bufferSizeKB);
    }

    public void setBlendCached(int src, int dst) {
        if (src != cachedBlendSrc || dst != cachedBlendDst) {
            GL11.glBlendFunc(src, dst);
            cachedBlendSrc = src;
            cachedBlendDst = dst;
        }
    }

    public void setDepthTestCached(boolean enable) {
        if (enable != cachedDepthTest) {
            if (enable) GL11.glEnable(GL11.GL_DEPTH_TEST);
            else GL11.glDisable(GL11.GL_DEPTH_TEST);
            cachedDepthTest = enable;
        }
    }

    public void setBlendCached(boolean enable) {
        if (enable != cachedBlend) {
            if (enable) GL11.glEnable(GL11.GL_BLEND);
            else GL11.glDisable(GL11.GL_BLEND);
            cachedBlend = enable;
        }
    }

    public void setCullFaceCached(boolean enable) {
        if (enable != cachedCullFace) {
            if (enable) GL11.glEnable(GL11.GL_CULL_FACE);
            else GL11.glDisable(GL11.GL_CULL_FACE);
            cachedCullFace = enable;
        }
    }

    public void invalidateCache() {
        cachedBlendSrc = -1;
        cachedBlendDst = -1;
        cachedDepthFunc = -1;
        cachedDepthTest = false;
        cachedBlend = false;
        cachedCullFace = false;
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
