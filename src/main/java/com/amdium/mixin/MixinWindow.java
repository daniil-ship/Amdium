package com.amdium.mixin;

import com.amdium.Amdium;
import com.amdium.config.AmdiumConfig;
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class MixinWindow {

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void amdium$onWindowInit(CallbackInfo ci) {
        Amdium.LOGGER.info("Window created — Amdium ready to optimize");
    }
}