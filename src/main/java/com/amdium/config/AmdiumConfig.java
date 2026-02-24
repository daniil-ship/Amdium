package com.amdium.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class AmdiumConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.BooleanValue FORCE_ENABLE;
    public static final ForgeConfigSpec.BooleanValue SHOW_HUD;

    public static final ForgeConfigSpec.BooleanValue CHUNK_OPTIMIZATION;
    public static final ForgeConfigSpec.BooleanValue SHADER_OPTIMIZATION;
    public static final ForgeConfigSpec.BooleanValue MEMORY_OPTIMIZATION;
    public static final ForgeConfigSpec.BooleanValue THREAD_OPTIMIZATION;
    public static final ForgeConfigSpec.BooleanValue APU_OPTIMIZATION;
    public static final ForgeConfigSpec.BooleanValue PIPELINE_OPTIMIZATION;
    public static final ForgeConfigSpec.BooleanValue AGGRESSIVE_MODE;
    public static final ForgeConfigSpec.BooleanValue SMART_VRAM;

    public static final ForgeConfigSpec.IntValue CHUNK_BUILD_THREADS;
    public static final ForgeConfigSpec.IntValue TARGET_FPS;
    public static final ForgeConfigSpec.BooleanValue ADAPTIVE_RENDER_DISTANCE;
    public static final ForgeConfigSpec.IntValue MIN_RENDER_DISTANCE;
    public static final ForgeConfigSpec.IntValue MAX_RENDER_DISTANCE;

    public static final ForgeConfigSpec.IntValue APU_VRAM_LIMIT_MB;
    public static final ForgeConfigSpec.BooleanValue APU_SHARED_MEMORY_OPT;
    public static final ForgeConfigSpec.EnumValue<APUProfile> APU_PROFILE;

    public static final ForgeConfigSpec.BooleanValue GL_BUFFER_OPTIMIZATION;
    public static final ForgeConfigSpec.BooleanValue GL_STATE_CACHE;
    public static final ForgeConfigSpec.BooleanValue BATCH_DRAW_CALLS;
    public static final ForgeConfigSpec.IntValue GL_BUFFER_SIZE_KB;

    public enum APUProfile {
        AUTO,
        POWER_SAVE,
        BALANCED,
        PERFORMANCE,
        ULTRA_PERFORMANCE
    }

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Amdium - AMD Optimizer Configuration").push("general");

        ENABLED = builder
                .comment("Enable/disable all Amdium optimizations")
                .define("enabled", true);

        FORCE_ENABLE = builder
                .comment("Force enable optimizations even on non-AMD hardware (for testing)")
                .define("force_enable", false);

        SHOW_HUD = builder
                .comment("Show performance HUD overlay")
                .define("show_hud", true);

        builder.pop();

        builder.comment("Optimization Modules").push("optimizations");

        CHUNK_OPTIMIZATION = builder
                .comment("Optimize chunk rendering for AMD GPUs")
                .define("chunk_optimization", true);

        SHADER_OPTIMIZATION = builder
                .comment("Optimize shader compilation and execution for AMD drivers")
                .define("shader_optimization", true);

        MEMORY_OPTIMIZATION = builder
                .comment("Optimize memory allocation patterns for AMD architecture")
                .define("memory_optimization", true);

        THREAD_OPTIMIZATION = builder
                .comment("Optimize thread distribution for AMD CPU topology (CCX/CCD)")
                .define("thread_optimization", true);

        APU_OPTIMIZATION = builder
                .comment("Enable special optimizations for AMD APUs (integrated graphics)")
                .define("apu_optimization", true);

        PIPELINE_OPTIMIZATION = builder
                .comment("Optimize the render pipeline for AMD GCN/RDNA architecture")
                .define("pipeline_optimization", true);

        AGGRESSIVE_MODE = builder
                .comment("Enable aggressive optimizations (may cause visual artifacts)")
                .define("aggressive_mode", false);

        SMART_VRAM = builder
                .comment("Enable smart VRAM management")
                .define("smart_vram", true);

        builder.pop();

        builder.comment("Render Settings").push("render");

        CHUNK_BUILD_THREADS = builder
                .comment("Number of threads for chunk building (0 = auto)")
                .defineInRange("chunk_build_threads", 0, 0, 32);

        TARGET_FPS = builder
                .comment("Target FPS for adaptive optimizations (0 = unlimited)")
                .defineInRange("target_fps", 60, 0, 300);

        ADAPTIVE_RENDER_DISTANCE = builder
                .comment("Dynamically adjust render distance based on FPS")
                .define("adaptive_render_distance", false);

        MIN_RENDER_DISTANCE = builder
                .comment("Minimum render distance for adaptive mode")
                .defineInRange("min_render_distance", 4, 2, 32);

        MAX_RENDER_DISTANCE = builder
                .comment("Maximum render distance for adaptive mode")
                .defineInRange("max_render_distance", 16, 4, 64);

        builder.pop();

        builder.comment("APU-specific Settings").push("apu");

        APU_VRAM_LIMIT_MB = builder
                .comment("VRAM limit for APU in MB (0 = auto)")
                .defineInRange("vram_limit_mb", 0, 0, 8192);

        APU_SHARED_MEMORY_OPT = builder
                .comment("Optimize shared memory access patterns for APU")
                .define("shared_memory_optimization", true);

        APU_PROFILE = builder
                .comment("APU optimization profile")
                .defineEnum("profile", APUProfile.AUTO);

        builder.pop();

        builder.comment("OpenGL Optimization Settings").push("opengl");

        GL_BUFFER_OPTIMIZATION = builder
                .comment("Optimize OpenGL buffer usage for AMD drivers")
                .define("buffer_optimization", true);

        GL_STATE_CACHE = builder
                .comment("Cache OpenGL state to reduce redundant calls")
                .define("state_cache", true);

        BATCH_DRAW_CALLS = builder
                .comment("Batch draw calls to reduce driver overhead")
                .define("batch_draw_calls", true);

        GL_BUFFER_SIZE_KB = builder
                .comment("GL buffer size in KB")
                .defineInRange("buffer_size_kb", 256, 64, 4096);

        builder.pop();

        SPEC = builder.build();
    }
}
