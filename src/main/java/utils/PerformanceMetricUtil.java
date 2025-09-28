package utils;

import com.e2e_activecare.config.TestConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static com.e2e_activecare.context.TestContextHolder.getCurrentTestName;
import static com.e2e_activecare.utils.BusinessLogicUtil.getNumberOfDevices;

public final class PerformanceMetricUtil {

    private PerformanceMetricUtil() {}
    private static final Logger LOGGER = LogManager.getLogger(PerformanceMetricUtil.class);

    private static final int DEVICE_LOAD = getNumberOfDevices();
    private static final int maxRunCount = TestConfig.getBoolean("loopRun") ? TestConfig.getInt("loopCount") : 1;

    private static final ThreadLocal<Map<String, Long>> START_TIMES = ThreadLocal.withInitial(HashMap::new);
    private static final ThreadLocal<Map<String, Long>> DURATIONS = ThreadLocal.withInitial(LinkedHashMap::new);
    private static final ThreadLocal<List<Map<String, Long>>> ALL_ITERATION_METRICS = ThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<Integer> PASS_COUNT = ThreadLocal.withInitial(() -> 0);
    private static final ThreadLocal<Integer> FAIL_COUNT = ThreadLocal.withInitial(() -> 0);

    public static void startTimer(String operation) {
        START_TIMES.get().put(operation, System.currentTimeMillis());
    }

    public static void endTimer(String operation) {
        Long startTime = START_TIMES.get().get(operation);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            DURATIONS.get().put(operation, duration);
        }
    }

    public static Map<String, Long> getDurations() {
        return new LinkedHashMap<>(DURATIONS.get());
    }

    public static void initializeMetricsCollection() {
        ALL_ITERATION_METRICS.get().clear();
        PASS_COUNT.set(0);
        FAIL_COUNT.set(0);
    }

    public static void collectIterationMetrics() {
        Map<String, Long> iterationMetrics = getDurations();
        ALL_ITERATION_METRICS.get().add(new LinkedHashMap<>(iterationMetrics));
        clearPerformanceMetrics();
    }

    public static void recordIterationPass(int iteration) {
        PASS_COUNT.set(PASS_COUNT.get() + 1);
        LOGGER.info("Iteration {}/{} completed successfully", iteration, maxRunCount);
    }

    public static void recordIterationFail(int iteration, Exception e) {
        FAIL_COUNT.set(FAIL_COUNT.get() + 1);
        LOGGER.error("Iteration {}/{} failed: {}", iteration, maxRunCount, e.getMessage());
    }

    public static void printExecutionSummaryAndCleanup() {
        printPerformanceSummary(ALL_ITERATION_METRICS.get());
        printPassFailSummary();
        ALL_ITERATION_METRICS.remove();
        PASS_COUNT.remove();
        FAIL_COUNT.remove();
    }

    public static void printPassFailSummary() {
        int passed = PASS_COUNT.get();
        int failed = FAIL_COUNT.get();
        int total = passed + failed;

        StringBuilder summary = new StringBuilder();
        summary.append("\n=== ITERATION PASS/FAIL SUMMARY ===\n");
        summary.append("Total Iterations: ").append(total).append("\n");
        summary.append("Passed: ").append(passed).append("\n");
        summary.append("Failed: ").append(failed).append("\n");

        if (failed > 0) {
            summary.append("TEST FAILED: ").append(failed).append(" out of ").append(total).append(" iterations failed");
            summary.append("=".repeat(70)).append("\n");
            LOGGER.info(summary.toString());
            throw new AssertionError("Test failed due to " + failed + " failed iterations");
        } else {
            summary.append("TEST PASSED: All iterations completed successfully\n");
            summary.append("=".repeat(70)).append("\n");
            LOGGER.info(summary.toString());
        }
    }

    public static void printPerformanceSummary(List<Map<String, Long>> allIterationMetrics) {
        String testName = getCurrentTestName();

        StringBuilder summary = new StringBuilder();
        summary.append("\n").append("=".repeat(70)).append("\n");
        summary.append(testName).append(" : TIME TAKEN FOR KEY OPERATIONS, WITH ").append(DEVICE_LOAD).append(" DEVICES LOAD\n");
        summary.append("=".repeat(70)).append("\n");

        for (int i = 0; i < allIterationMetrics.size(); i++) {
            Map<String, Long> metrics = allIterationMetrics.get(i);
            if (!metrics.isEmpty()) {
                summary.append("Iteration ").append(i + 1).append(":\n");
                metrics.forEach((operation, duration) -> {
                    long totalSeconds = duration / 1000;
                    long minutes = totalSeconds / 60;
                    long seconds = totalSeconds % 60;
                    summary.append(String.format("  %-30s: %d minute %d seconds%n", operation, minutes, seconds));
                });
                summary.append("-".repeat(50)).append("\n");
            }
        }

        summary.append(buildAveragesSummary(allIterationMetrics, testName));
        summary.append("=".repeat(70)).append("\n");

        LOGGER.info(summary.toString());
    }

    private static String buildAveragesSummary(List<Map<String, Long>> allIterationMetrics, String testName) {
        Map<String, List<Long>> operationTimes = new LinkedHashMap<>();

        for (Map<String, Long> iterationMetrics : allIterationMetrics) {
            iterationMetrics.forEach((operation, duration) -> {
                operationTimes.computeIfAbsent(operation, k -> new ArrayList<>()).add(duration);
            });
        }

        if (operationTimes.isEmpty()) {
            return "";
        }

        StringBuilder avgSummary = new StringBuilder();
        avgSummary.append("\n").append(testName).append(" : AVERAGE TIME ACROSS ALL ").append(allIterationMetrics.size()).append(" ITERATIONS WITH ").append(DEVICE_LOAD).append(" DEVICES LOAD\n");
        avgSummary.append("-".repeat(80)).append("\n");

        operationTimes.forEach((operation, times) -> {
            double avgMillis = times.stream().mapToLong(Long::longValue).average().orElse(0);
            long avgTotalSeconds = Math.round(avgMillis / 1000);
            long avgMinutes = avgTotalSeconds / 60;
            long avgSeconds = avgTotalSeconds % 60;
            avgSummary.append(String.format("%-35s: %d minute %d seconds%n", operation, avgMinutes, avgSeconds));
        });
        avgSummary.append("\n");
        return avgSummary.toString();
    }

    public static void clearPerformanceMetrics() {
        START_TIMES.get().clear();
        DURATIONS.get().clear();
    }

    public static void clear() {
        START_TIMES.remove();
        DURATIONS.remove();
        ALL_ITERATION_METRICS.remove();
        PASS_COUNT.remove();
        FAIL_COUNT.remove();
        LOGGER.info("ThreadLocal - performance metrics cleared from listener");
    }
}