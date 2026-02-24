package com.amdium.util;

import com.amdium.Amdium;

public class Logger {

    public static void info(String msg, Object... args) {
        Amdium.LOGGER.info("[Amdium] " + msg, args);
    }

    public static void warn(String msg, Object... args) {
        Amdium.LOGGER.warn("[Amdium] " + msg, args);
    }

    public static void error(String msg, Object... args) {
        Amdium.LOGGER.error("[Amdium] " + msg, args);
    }

    public static void debug(String msg, Object... args) {
        Amdium.LOGGER.debug("[Amdium] " + msg, args);
    }

    public static void hardware(String msg, Object... args) {
        Amdium.LOGGER.info("[Amdium/HW] " + msg, args);
    }

    public static void optimization(String msg, Object... args) {
        Amdium.LOGGER.info("[Amdium/OPT] " + msg, args);
    }

    public static void benchmark(String msg, Object... args) {
        Amdium.LOGGER.info("[Amdium/BENCH] " + msg, args);
    }
}
