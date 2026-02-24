package com.amdium;

import com.amdium.config.AmdiumConfig;
import com.amdium.core.AMDDetector;
import com.amdium.core.AMDOptimizer;
import com.amdium.core.CPUInfo;
import com.amdium.core.GPUInfo;
import com.amdium.gui.AmdiumHudOverlay;
import com.amdium.gui.AmdiumSettingsScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Amdium.MOD_ID)
public class Amdium {

    public static final String MOD_ID = "amdium";
    public static final String MOD_NAME = "Amdium";
    public static final String VERSION = "1.0.0";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    private static Amdium instance;
    private AMDDetector detector;
    private AMDOptimizer optimizer;
    private GPUInfo gpuInfo;
    private CPUInfo cpuInfo;
    private boolean amdDetected = false;
    private boolean apuDetected = false;
    private boolean optimizationsApplied = false;

    public static KeyMapping keyOpenSettings;
    public static KeyMapping keyToggleHud;

    private Minecraft minecraft;

    public Amdium() {
        instance = this;

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, AmdiumConfig.SPEC, "amdium-client.toml");

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modBus.addListener(this::onClientSetup);
            modBus.addListener(this::onRegisterKeyMappings);
            modBus.addListener(this::onRegisterGuiOverlays);
            MinecraftForge.EVENT_BUS.register(this);
        }

        LOGGER.info("===========================================");
        LOGGER.info("  Amdium v{} - AMD Optimizer for Minecraft", VERSION);
        LOGGER.info("  Optimizing for AMD CPUs, APUs & Radeon GPUs");
        LOGGER.info("===========================================");
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            try {
                detector = new AMDDetector();
                gpuInfo = detector.detectGPU();
                cpuInfo = detector.detectCPU();

                amdDetected = gpuInfo.isAMD() || cpuInfo.isAMD();
                apuDetected = detector.isAPU();

                LOGGER.info("Hardware Detection Results:");
                LOGGER.info("  GPU: {} (AMD: {})", gpuInfo.getName(), gpuInfo.isAMD());
                LOGGER.info("  CPU: {} (AMD: {})", cpuInfo.getName(), cpuInfo.isAMD());
                LOGGER.info("  APU Detected: {}", apuDetected);
                LOGGER.info("  VRAM: {} MB", gpuInfo.getVramMB());
                LOGGER.info("  CPU Cores: {} (Threads: {})", cpuInfo.getCores(), cpuInfo.getThreads());

                if (amdDetected || AmdiumConfig.FORCE_ENABLE.get()) {
                    optimizer = new AMDOptimizer(gpuInfo, cpuInfo, apuDetected);
                    optimizer.applyOptimizations();
                    optimizationsApplied = true;
                    LOGGER.info("AMD optimizations applied successfully!");
                } else {
                    LOGGER.info("No AMD hardware detected. Use force_enable in config to override.");
                }
            } catch (Exception e) {
                LOGGER.error("Failed to initialize: {}", e.getMessage());
            }
        });
    }

    private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        keyOpenSettings = new KeyMapping(
                "amdium.key.settings",
                GLFW.GLFW_KEY_F9,
                "amdium.key.category"
        );
        keyToggleHud = new KeyMapping(
                "amdium.key.toggle_hud",
                GLFW.GLFW_KEY_F10,
                "amdium.key.category"
        );
        event.register(keyOpenSettings);
        event.register(keyToggleHud);
    }

    private void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(
                VanillaGuiOverlay.DEBUG_TEXT.id(),
                "amdium_hud",
                new AmdiumHudOverlay()
        );
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        try {
            if (minecraft == null) {
                minecraft = net.minecraft.client.Minecraft.getInstance();
            }

            if (minecraft == null || minecraft.player == null) return;

            if (keyOpenSettings != null) {
                while (keyOpenSettings.consumeClick()) {
                    minecraft.setScreen(new AmdiumSettingsScreen(minecraft.screen));
                }
            }

            if (keyToggleHud != null) {
                while (keyToggleHud.consumeClick()) {
                    AmdiumConfig.SHOW_HUD.set(!AmdiumConfig.SHOW_HUD.get());
                }
            }

            if (optimizationsApplied && optimizer != null) {
                optimizer.tick();
            }
        } catch (Exception e) {
          
        }
    }

    public static Amdium getInstance() {
        return instance;
    }

    public GPUInfo getGpuInfo() {
        return gpuInfo;
    }

    public CPUInfo getCpuInfo() {
        return cpuInfo;
    }

    public boolean isAmdDetected() {
        return amdDetected;
    }

    public boolean isApuDetected() {
        return apuDetected;
    }

    public boolean isOptimizationsApplied() {
        return optimizationsApplied;
    }
}
