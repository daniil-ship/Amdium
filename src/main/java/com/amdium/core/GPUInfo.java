package com.amdium.core;

public class GPUInfo {

    private final String name;
    private final String vendor;
    private final String renderer;
    private final String driverVersion;
    private final String glVersion;
    private final long vramBytes;
    private final boolean isAMD;
    private final boolean isRadeon;
    private final boolean isIntegrated;
    private final GPUArchitecture architecture;

    public enum GPUArchitecture {
        UNKNOWN,
        GCN_1,      // HD 7000, R7/R9 200
        GCN_2,      // R7/R9 300 (Hawaii, Bonaire)
        GCN_3,      // R9 285, 380 (Tonga)
        GCN_4,      // RX 400 (Polaris)
        GCN_5,      // Vega
        RDNA_1,     // RX 5000
        RDNA_2,     // RX 6000
        RDNA_3,     // RX 7000
        RDNA_3_5,   // RX 7000 refresh / APU
        VEGA_APU,   // Ryzen 2000-3000 APU (Vega)
        RDNA_2_APU, // Ryzen 6000 APU
        RDNA_3_APU  // Ryzen 7000/8000 APU
    }

    public GPUInfo(String name, String vendor, String renderer, String driverVersion,
                   String glVersion, long vramBytes, boolean isAMD, boolean isRadeon,
                   boolean isIntegrated, GPUArchitecture architecture) {
        this.name = name;
        this.vendor = vendor;
        this.renderer = renderer;
        this.driverVersion = driverVersion;
        this.glVersion = glVersion;
        this.vramBytes = vramBytes;
        this.isAMD = isAMD;
        this.isRadeon = isRadeon;
        this.isIntegrated = isIntegrated;
        this.architecture = architecture;
    }

    public String getName() { return name; }
    public String getVendor() { return vendor; }
    public String getRenderer() { return renderer; }
    public String getDriverVersion() { return driverVersion; }
    public String getGlVersion() { return glVersion; }
    public long getVramBytes() { return vramBytes; }
    public int getVramMB() { return (int)(vramBytes / (1024 * 1024)); }
    public boolean isAMD() { return isAMD; }
    public boolean isRadeon() { return isRadeon; }
    public boolean isIntegrated() { return isIntegrated; }
    public GPUArchitecture getArchitecture() { return architecture; }

    public boolean isRDNA() {
        return architecture == GPUArchitecture.RDNA_1 ||
                architecture == GPUArchitecture.RDNA_2 ||
                architecture == GPUArchitecture.RDNA_3 ||
                architecture == GPUArchitecture.RDNA_3_5 ||
                architecture == GPUArchitecture.RDNA_2_APU ||
                architecture == GPUArchitecture.RDNA_3_APU;
    }

    public boolean isGCN() {
        return architecture == GPUArchitecture.GCN_1 ||
                architecture == GPUArchitecture.GCN_2 ||
                architecture == GPUArchitecture.GCN_3 ||
                architecture == GPUArchitecture.GCN_4 ||
                architecture == GPUArchitecture.GCN_5 ||
                architecture == GPUArchitecture.VEGA_APU;
    }

    public boolean isVega() {
        return architecture == GPUArchitecture.GCN_5 ||
                architecture == GPUArchitecture.VEGA_APU;
    }

    @Override
    public String toString() {
        return String.format("GPUInfo{name='%s', vram=%dMB, arch=%s, integrated=%s}",
                name, getVramMB(), architecture, isIntegrated);
    }
}