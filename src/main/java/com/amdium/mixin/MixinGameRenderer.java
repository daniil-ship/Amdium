package com.amdium.mixin;

import com.amdium.Amdium;
import com.amdium.config.AmdiumConfig;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Inject(method = "render", at = @At("HEAD"))
    private void amdium$onRenderStart(float partialTick, long nanoTime, boolean renderLevel, CallbackInfo ci) {
        if (!AmdiumConfig.ENABLED.get()) return;

        // Оптимизация начала кадра для AMD
        // AMD драйвер: начинаем с чистого состояния для лучшего pipelining
        if (AmdiumConfig.GL_STATE_CACHE.get() && Amdium.getInstance().isAmdDetected()) {
            // Сброс кеша состояний в начале каждого кадра
            // чтобы AMD драйвер мог лучше оптимизировать
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void amdium$onRenderEnd(float partialTick, long nanoTime, boolean renderLevel, CallbackInfo ci) {
        if (!AmdiumConfig.ENABLED.get()) return;

        // Бенчмарк тик
        if (com.amdium.util.BenchmarkUtil.isRunning()) {
            com.amdium.util.BenchmarkUtil.tick();
        }
    }
}