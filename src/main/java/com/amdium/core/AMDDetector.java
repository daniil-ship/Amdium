package com.amdium.core;

import com.amdium.Amdium;
import org.lwjgl.opengl.GL11;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Locale;

public class AMDDetector {

    private GPUInfo gpuInfo;
    private CPUInfo cpuInfo;
    private boolean isAPU;

    public GPUInfo detectGPU() {
        try {
            String vendor = GL11.glGetString(GL11.GL_VENDOR);
            String renderer = GL11.glGetString(GL11.GL_RENDERER);
            String glVersion = GL11.glGetString(GL11.GL_VERSION);

            if (vendor == null) vendor = "Unknown";
            if (renderer == null) renderer = "Unknown";
            if (glVersion == null) glVersion = "Unknown";

            String vendorLower = vendor.toLowerCase(Locale.ROOT);
            String rendererLower = renderer.toLowerCase(Locale.ROOT);

            boolean isAMD = vendorLower.contains("amd") ||
                    vendorLower.contains("ati") ||
                    vendorLower.contains("advanced micro devices");

            boolean isNvidia = vendorLower.contains("nvidia");
            boolean isIntel = vendorLower.contains("intel");

            if (isNvidia || isIntel) {
                isAMD = false;
            }

            boolean isRadeon = rendererLower.contains("radeon");

            if (isRadeon) {
                isAMD = true;
            }

            boolean isIntegrated = rendererLower.contains("vega") && rendererLower.contains("graphics") ||
                    rendererLower.contains("radeon(tm) graphics") ||
                    rendererLower.contains("radeon graphics") ||
                    rendererLower.contains("radeon 680m") ||
                    rendererLower.contains("radeon 780m") ||
                    rendererLower.contains("radeon 760m") ||
                    rendererLower.contains("radeon 890m");

            String driverVersion = extractDriverVersion(glVersion);
            long vramBytes = detectVRAM(rendererLower);
            GPUInfo.GPUArchitecture arch = isAMD ? detectGPUArchitecture(rendererLower) : GPUInfo.GPUArchitecture.UNKNOWN;

            gpuInfo = new GPUInfo(renderer, vendor, renderer, driverVersion,
                    glVersion, vramBytes, isAMD, isRadeon, isIntegrated, arch);

            Amdium.LOGGER.info("GPU detected: {}", gpuInfo);
            Amdium.LOGGER.info("  Vendor: {}", vendor);
            Amdium.LOGGER.info("  Is AMD: {}", isAMD);
            Amdium.LOGGER.info("  Is Nvidia: {}", isNvidia);
            Amdium.LOGGER.info("  Is Intel: {}", isIntel);

            return gpuInfo;

        } catch (Exception e) {
            Amdium.LOGGER.error("Failed to detect GPU", e);
            return new GPUInfo("Unknown", "Unknown", "Unknown", "Unknown",
                    "Unknown", 0, false, false, false,
                    GPUInfo.GPUArchitecture.UNKNOWN);
        }
    }

    public CPUInfo detectCPU() {
        try {
            String cpuName = getCPUName();
            String cpuNameLower = cpuName.toLowerCase(Locale.ROOT);

            boolean isAMD = cpuNameLower.contains("amd") || cpuNameLower.contains("ryzen");

            boolean isIntel = cpuNameLower.contains("intel") || cpuNameLower.contains("core");
            if (isIntel) {
                isAMD = false;
            }

            int cores = Runtime.getRuntime().availableProcessors();
            int physicalCores = detectPhysicalCores();
            if (physicalCores <= 0) physicalCores = cores / 2;

            CPUInfo.CPUArchitecture arch = isAMD ? detectCPUArchitecture(cpuNameLower) : CPUInfo.CPUArchitecture.UNKNOWN;
            boolean hasAPU = isAMD && isAPUProcessor(cpuNameLower);

            cpuInfo = new CPUInfo(cpuName, isAMD, physicalCores, cores, arch, hasAPU);

            Amdium.LOGGER.info("CPU detected: {}", cpuInfo);
            return cpuInfo;

        } catch (Exception e) {
            Amdium.LOGGER.error("Failed to detect CPU", e);
            int threads = Runtime.getRuntime().availableProcessors();
            return new CPUInfo("Unknown", false, threads / 2, threads,
                    CPUInfo.CPUArchitecture.UNKNOWN, false);
        }
    }

    public boolean isAPU() {
        if (gpuInfo == null || cpuInfo == null) return false;
        isAPU = cpuInfo.hasAPU() || gpuInfo.isIntegrated();
        return isAPU;
    }

    private GPUInfo.GPUArchitecture detectGPUArchitecture(String renderer) {
        // RDNA 3.5 / APU
        if (renderer.contains("890m") || renderer.contains("radeon ai")) {
            return GPUInfo.GPUArchitecture.RDNA_3_APU;
        }
        // RDNA 3
        if (renderer.contains("rx 7") || renderer.contains("7900") ||
                renderer.contains("7800") || renderer.contains("7700") ||
                renderer.contains("7600")) {
            return GPUInfo.GPUArchitecture.RDNA_3;
        }
        // RDNA 3 APU
        if (renderer.contains("780m") || renderer.contains("760m")) {
            return GPUInfo.GPUArchitecture.RDNA_3_APU;
        }
        // RDNA 2
        if (renderer.contains("rx 6") || renderer.contains("6900") ||
                renderer.contains("6800") || renderer.contains("6700") ||
                renderer.contains("6600") || renderer.contains("6500") ||
                renderer.contains("6400")) {
            return GPUInfo.GPUArchitecture.RDNA_2;
        }
        // RDNA 2 APU
        if (renderer.contains("680m") || renderer.contains("660m")) {
            return GPUInfo.GPUArchitecture.RDNA_2_APU;
        }
        // RDNA 1
        if (renderer.contains("rx 5") || renderer.contains("5700") ||
                renderer.contains("5600") || renderer.contains("5500")) {
            return GPUInfo.GPUArchitecture.RDNA_1;
        }
        // Vega
        if (renderer.contains("vega") || renderer.contains("radeon vii")) {
            if (renderer.contains("graphics") || renderer.contains("mobile")) {
                return GPUInfo.GPUArchitecture.VEGA_APU;
            }
            return GPUInfo.GPUArchitecture.GCN_5;
        }
        // Radeon Graphics (generic APU)
        if (renderer.contains("radeon(tm) graphics") || renderer.contains("radeon graphics")) {
            return GPUInfo.GPUArchitecture.VEGA_APU;
        }
        // Polaris / GCN 4
        if (renderer.contains("rx 4") || renderer.contains("480") ||
                renderer.contains("470") || renderer.contains("460") ||
                renderer.contains("580") || renderer.contains("570") ||
                renderer.contains("560") || renderer.contains("550")) {
            return GPUInfo.GPUArchitecture.GCN_4;
        }

        return GPUInfo.GPUArchitecture.UNKNOWN;
    }

    private CPUInfo.CPUArchitecture detectCPUArchitecture(String cpuName) {
        // Zen 5
        if (cpuName.contains("9950") || cpuName.contains("9900") ||
                cpuName.contains("9700") || cpuName.contains("9600")) {
            return CPUInfo.CPUArchitecture.ZEN_5;
        }
        // Zen 4
        if (cpuName.contains("7950") || cpuName.contains("7900") ||
                cpuName.contains("7800") || cpuName.contains("7700") ||
                cpuName.contains("7600") || cpuName.contains("7500")) {
            return CPUInfo.CPUArchitecture.ZEN_4;
        }
        // Zen 3
        if (cpuName.contains("5950") || cpuName.contains("5900") ||
                cpuName.contains("5800") || cpuName.contains("5700") ||
                cpuName.contains("5600") || cpuName.contains("5500")) {
            return CPUInfo.CPUArchitecture.ZEN_3;
        }
        // Zen 2
        if (cpuName.contains("3950") || cpuName.contains("3900") ||
                cpuName.contains("3800") || cpuName.contains("3700") ||
                cpuName.contains("3600") || cpuName.contains("3500")) {
            return CPUInfo.CPUArchitecture.ZEN_2;
        }
        // Zen+
        if (cpuName.contains("2700") || cpuName.contains("2600") ||
                cpuName.contains("2500") || cpuName.contains("2400") ||
                cpuName.contains("2200")) {
            return CPUInfo.CPUArchitecture.ZEN_PLUS;
        }
        // Zen 1
        if (cpuName.contains("1800") || cpuName.contains("1700") ||
                cpuName.contains("1600") || cpuName.contains("1500") ||
                cpuName.contains("1400") || cpuName.contains("1300")) {
            return CPUInfo.CPUArchitecture.ZEN_1;
        }

        return CPUInfo.CPUArchitecture.UNKNOWN;
    }

    private boolean isAPUProcessor(String cpuName) {
        return cpuName.matches(".*\\d{4}[gG].*") ||  // 5600G, 3400G
                cpuName.contains("with radeon") ||
                cpuName.matches(".*ryzen.*[hHuU]$");   // Mobile APU
    }

    private String getCPUName() {
        String os = System.getProperty("os.name", "").toLowerCase();

        try {
            ProcessBuilder pb;
            if (os.contains("win")) {
                pb = new ProcessBuilder("wmic", "cpu", "get", "Name");
            } else if (os.contains("linux")) {
                pb = new ProcessBuilder("bash", "-c",
                        "cat /proc/cpuinfo | grep 'model name' | head -1 | cut -d ':' -f2");
            } else {
                return System.getProperty("os.arch", "Unknown CPU");
            }

            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder result = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.equalsIgnoreCase("Name")) {
                    result.append(line);
                }
            }
            process.waitFor();
            String cpuName = result.toString().trim();
            return cpuName.isEmpty() ? "Unknown CPU" : cpuName;

        } catch (Exception e) {
            Amdium.LOGGER.warn("Could not detect CPU name: {}", e.getMessage());
            return "Unknown CPU";
        }
    }

    private int detectPhysicalCores() {
        String os = System.getProperty("os.name", "").toLowerCase();
        try {
            ProcessBuilder pb;
            if (os.contains("win")) {
                pb = new ProcessBuilder("wmic", "cpu", "get", "NumberOfCores");
            } else if (os.contains("linux")) {
                pb = new ProcessBuilder("bash", "-c",
                        "cat /proc/cpuinfo | grep 'cpu cores' | head -1 | cut -d ':' -f2");
            } else {
                return -1;
            }

            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && line.matches("\\d+")) {
                    return Integer.parseInt(line);
                }
            }
        } catch (Exception e) {
            Amdium.LOGGER.warn("Could not detect physical cores: {}", e.getMessage());
        }
        return -1;
    }

    private long detectVRAM(String renderer) {
        return estimateVRAMFromRenderer(renderer);
    }

    private long estimateVRAMFromRenderer(String renderer) {
        if (renderer == null) return 2L * 1024 * 1024 * 1024;

        String r = renderer.toLowerCase();
      
        // RDNA 3
        if (r.contains("7900 xtx")) return 24L * 1024 * 1024 * 1024;
        if (r.contains("7900 xt")) return 20L * 1024 * 1024 * 1024;
        if (r.contains("7800 xt")) return 16L * 1024 * 1024 * 1024;
        if (r.contains("7700 xt")) return 12L * 1024 * 1024 * 1024;
        if (r.contains("7600")) return 8L * 1024 * 1024 * 1024;

        if (r.contains("6900")) return 16L * 1024 * 1024 * 1024;
        if (r.contains("6800")) return 16L * 1024 * 1024 * 1024;
        if (r.contains("6700 xt")) return 12L * 1024 * 1024 * 1024;
        if (r.contains("6600")) return 8L * 1024 * 1024 * 1024;

        if (r.contains("rtx 4090")) return 24L * 1024 * 1024 * 1024;
        if (r.contains("rtx 4080")) return 16L * 1024 * 1024 * 1024;
        if (r.contains("rtx 3080")) return 10L * 1024 * 1024 * 1024;
        if (r.contains("gtx 1650")) return 4L * 1024 * 1024 * 1024;

        return 4L * 1024 * 1024 * 1024; // 4GB default
    }

    private String extractDriverVersion(String glVersion) {
        if (glVersion == null) return "Unknown";
        return glVersion;
    }
}
