package com.amdium.mixin;

import com.amdium.config.AmdiumConfig;
import net.minecraft.client.renderer.LevelRenderer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void amdium$beforeRenderLevel(CallbackInfo ci) {
        if (!AmdiumConfig.ENABLED.get()) return;
        if (AmdiumConfig.PIPELINE_OPTIMIZATION.get()) {
            GL11.glDisable(GL11.GL_DITHER);
        }
    }

}
