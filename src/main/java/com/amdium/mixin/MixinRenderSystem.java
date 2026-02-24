package com.amdium.mixin;

import com.amdium.Amdium;
import com.amdium.config.AmdiumConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public class MixinRenderSystem {

    private static boolean amdium$initialized = false;

    @Inject(method = "initRenderer", at = @At("RETURN"), remap = false)
    private static void amdium$onInitRenderer(int debugVerbosity, boolean debugSync, CallbackInfo ci) {
        if (amdium$initialized) return;
        amdium$initialized = true;

        if (!AmdiumConfig.ENABLED.get()) return;

        Amdium.LOGGER.info("RenderSystem initialized — applying AMD render optimizations");
        try {
            org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_DITHER);

            if (AmdiumConfig.PIPELINE_OPTIMIZATION.get()) {
                org.lwjgl.opengl.GL11.glHint(
                        org.lwjgl.opengl.GL11.GL_LINE_SMOOTH_HINT,
                        org.lwjgl.opengl.GL11.GL_FASTEST);
                org.lwjgl.opengl.GL11.glHint(
                        org.lwjgl.opengl.GL11.GL_POLYGON_SMOOTH_HINT,
                        org.lwjgl.opengl.GL11.GL_FASTEST);
            }
        } catch (Exception e) {
            Amdium.LOGGER.debug("Early GL optimizations partially failed: {}", e.getMessage());
        }
    }

}
