package com.amdium.util;

import com.amdium.Amdium;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class BenchmarkUtil {

    private static boolean running = false;
    private static long startTime;
    private static final List<Integer> fpsHistory = new ArrayList<>();
    private static int duration = 10; // seconds

    public static void startBenchmark(int durationSeconds) {
        if (running) return;

        duration = durationSeconds;
        fpsHistory.clear();
        startTime = System.currentTimeMillis();
        running = true;
        Amdium.LOGGER.info("Benchmark started ({} seconds)", duration);
    }

    public static void tick() {
        if (!running) return;

        Minecraft mc = Minecraft.getInstance();
        fpsHistory.add(mc.getFps());

        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed >= duration * 1000L) {
            finishBenchmark();
        }
    }

    private static void finishBenchmark() {
        running = false;

        if (fpsHistory.isEmpty()) {
            Amdium.LOGGER.info("Benchmark failed: no data collected");
            return;
        }

        int sum = 0;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for (int fps : fpsHistory) {
            sum += fps;
            min = Math.min(min, fps);
            max = Math.max(max, fps);
        }

        int avg = sum / fpsHistory.size();

        // Рассчитываем 1% low и 0.1% low
        List<Integer> sorted = new ArrayList<>(fpsHistory);
        sorted.sort(Integer::compareTo);
        int onePercentLow = sorted.get(Math.max(0, (int)(sorted.size() * 0.01)));
        int pointOnePercentLow = sorted.get(0);

        Amdium.LOGGER.info("=== Benchmark Results ===");
        Amdium.LOGGER.info("  Duration: {} seconds", duration);
        Amdium.LOGGER.info("  Samples: {}", fpsHistory.size());
        Amdium.LOGGER.info("  Average FPS: {}", avg);
        Amdium.LOGGER.info("  Min FPS: {}", min);
        Amdium.LOGGER.info("  Max FPS: {}", max);
        Amdium.LOGGER.info("  1% Low: {}", onePercentLow);
        Amdium.LOGGER.info("  0.1% Low: {}", pointOnePercentLow);
        Amdium.LOGGER.info("========================");
    }

    public static boolean isRunning() {
        return running;
    }

    public static int getProgress() {
        if (!running) return 0;
        long elapsed = System.currentTimeMillis() - startTime;
        return (int) Math.min(100, elapsed * 100 / (duration * 1000L));
    }

    public static int getLastAverage() {
        if (fpsHistory.isEmpty()) return 0;
        int sum = 0;
        for (int fps : fpsHistory) sum += fps;
        return sum / fpsHistory.size();
    }
}