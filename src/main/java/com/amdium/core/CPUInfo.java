package com.amdium.core;

public class CPUInfo {

    private final String name;
    private final boolean isAMD;
    private final int cores;
    private final int threads;
    private final CPUArchitecture architecture;
    private final boolean hasAPU;

    public enum CPUArchitecture {
        UNKNOWN,
        ZEN_1,       // Ryzen 1000
        ZEN_PLUS,    // Ryzen 2000
        ZEN_2,       // Ryzen 3000
        ZEN_3,       // Ryzen 5000
        ZEN_3_PLUS,  // Ryzen 6000 mobile
        ZEN_4,       // Ryzen 7000
        ZEN_5,       // Ryzen 9000
        BULLDOZER,   // FX series
        PILEDRIVER,
        STEAMROLLER,
        EXCAVATOR
    }

    public CPUInfo(String name, boolean isAMD, int cores, int threads,
                   CPUArchitecture architecture, boolean hasAPU) {
        this.name = name;
        this.isAMD = isAMD;
        this.cores = cores;
        this.threads = threads;
        this.architecture = architecture;
        this.hasAPU = hasAPU;
    }

    public String getName() { return name; }
    public boolean isAMD() { return isAMD; }
    public int getCores() { return cores; }
    public int getThreads() { return threads; }
    public CPUArchitecture getArchitecture() { return architecture; }
    public boolean hasAPU() { return hasAPU; }

    public boolean isZen() {
        return architecture != CPUArchitecture.UNKNOWN &&
                architecture != CPUArchitecture.BULLDOZER &&
                architecture != CPUArchitecture.PILEDRIVER &&
                architecture != CPUArchitecture.STEAMROLLER &&
                architecture != CPUArchitecture.EXCAVATOR;
    }

    public int getOptimalChunkThreads() {
        if (!isAMD) return Math.max(1, threads / 2);

        return switch (architecture) {
            case ZEN_1, ZEN_PLUS -> Math.max(1, cores - 1);
            case ZEN_2 -> Math.max(2, cores - 1);
            case ZEN_3, ZEN_3_PLUS -> Math.max(2, (int)(cores * 0.75));
            case ZEN_4, ZEN_5 -> Math.max(2, (int)(cores * 0.8));
            case BULLDOZER, PILEDRIVER, STEAMROLLER, EXCAVATOR -> Math.max(1, cores / 2);
            default -> Math.max(1, threads / 2);
        };
    }

    public int getCCXThreads() {
        return switch (architecture) {
            case ZEN_1, ZEN_PLUS -> 4;
            case ZEN_2 -> 4;
            case ZEN_3, ZEN_3_PLUS -> 8;
            case ZEN_4, ZEN_5 -> 8;
            default -> cores;
        };
    }

    @Override
    public String toString() {
        return String.format("CPUInfo{name='%s', cores=%d, threads=%d, arch=%s, apu=%s}",
                name, cores, threads, architecture, hasAPU);
    }

}
