package com.amdium.mixin;

import com.amdium.Amdium;
import com.amdium.config.AmdiumConfig;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkRenderDispatcher.class)
public class MixinChunkRenderDispatcher {

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void amdium$onInit(CallbackInfo ci) {
        if (!AmdiumConfig.ENABLED.get() || !AmdiumConfig.CHUNK_OPTIMIZATION.get()) return;

        Amdium.LOGGER.debug("ChunkRenderDispatcher initialized with Amdium optimizations");
    }
}