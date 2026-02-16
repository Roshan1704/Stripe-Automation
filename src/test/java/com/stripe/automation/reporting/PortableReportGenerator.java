package com.stripe.automation.reporting;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public final class PortableReportGenerator {
    private static final List<String> STATUS_ORDER = List.of("failed", "broken", "skipped", "passed", "unknown");

    private PortableReportGenerator() {
    }

    public static void main(String[] args) throws Exception {
        Path resultsDir = Path.of(configOrDefault("ALLURE_RESULTS_PATH", "target/allure-results"));
        Path output = Path.of(configOrDefault("PORTABLE_REPORT_PATH", "target/site/allure-maven-plugin/portable-index.html"));
        boolean patchIndex = Boolean.parseBoolean(configOrDefault("PATCH_ALLURE_INDEX", "true"));

        Files.createDirectories(output.getParent());
        List<Row> rows = loadRows(resultsDir);
        String generatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Files.writeString(output, buildHtml(rows, generatedAt), StandardCharsets.UTF_8);

        if (patchIndex) {
            patchIndex(output.getParent());
        }

        System.out.println("Portable stakeholder report generated: " + output);
    }

    private static String configOrDefault(String key, String fallback) {
        String value = config(key);
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private static String config(String key) {
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp;
        }
        String env = System.getenv(key);
        if (env != null && !env.isBlank()) {
            return env;
        }
        return null;
    }

    private static List<Row> loadRows(Path resultsDir) throws IOException {
        if (!Files.exists(resultsDir)) {
            return List.of();
        }

        ObjectMapper mapper = new ObjectMapper();
        List<Row> rows = new ArrayList<>();

        try (Stream<Path> stream = Files.list(resultsDir)) {
            stream.filter(p -> p.getFileName().toString().endsWith("-result.json")).sorted().forEach(file -> {
                try {
                    JsonNode root = mapper.readTree(file.toFile());
                    String name = root.path("name").asText(file.getFileName().toString());
                    String status = root.path("status").asText("unknown");
                    long start = root.path("start").asLong(-1);
                    long stop = root.path("stop").asLong(-1);
                    Long durationMs = (start > 0 && stop > 0 && stop >= start) ? (stop - start) : null;

                    String suite = "-";
                    String pkg = "-";
                    for (JsonNode label : root.path("labels")) {
                        String ln = label.path("name").asText();
                        String lv = label.path("value").asText("-");
                        if ("suite".equals(ln) || "parentSuite".equals(ln)) {
                            suite = lv;
                        }
                        if ("package".equals(ln)) {
                            pkg = lv;
                        }
                    }

                    String error = root.path("statusDetails").path("message").asText("");
                    rows.add(new Row(name, status, suite, pkg, durationMs, error));
                } catch (Exception ignored) {
                }
            });
        }

        rows.sort(Comparator.comparingInt((Row r) -> {
            int i = STATUS_ORDER.indexOf(r.status);
            return i >= 0 ? i : 99;
        }).thenComparing((Row r) -> r.name));

        return rows;
    }

    private static String buildHtml(List<Row> rows, String generatedAt) {
        long passed = rows.stream().filter(r -> "passed".equals(r.status)).count();
        long failed = rows.stream().filter(r -> "failed".equals(r.status) || "broken".equals(r.status)).count();
        long skipped = rows.stream().filter(r -> "skipped".equals(r.status)).count();
        long total = rows.size();
        double passRate = total == 0 ? 0.0 : (passed * 100.0 / total);

        Set<String> uniqueTests = new HashSet<>();
        Set<String> uniqueFailedTests = new HashSet<>();
        for (Row r : rows) {
            uniqueTests.add(r.name);
            if ("failed".equals(r.status) || "broken".equals(r.status)) {
                uniqueFailedTests.add(r.name);
            }
        }

        Map<String, SuiteAgg> suiteAgg = new HashMap<>();
        for (Row r : rows) {
            SuiteAgg agg = suiteAgg.computeIfAbsent(r.suite, k -> new SuiteAgg());
            agg.total++;
            if ("passed".equals(r.status)) agg.passed++;
            else if ("skipped".equals(r.status)) agg.skipped++;
            else if ("failed".equals(r.status) || "broken".equals(r.status)) agg.failed++;
            else agg.unknown++;
        }

        List<Map.Entry<String, SuiteAgg>> suites = new ArrayList<>(suiteAgg.entrySet());
        suites.sort((a, b) -> Long.compare(b.getValue().total, a.getValue().total));

        List<Row> failedRows = rows.stream().filter(r -> "failed".equals(r.status) || "broken".equals(r.status)).limit(20).toList();

        StringBuilder suiteRows = new StringBuilder();
        if (suites.isEmpty()) {
            suiteRows.append("<tr><td colspan='6'>No suite data found.</td></tr>");
        } else {
            for (Map.Entry<String, SuiteAgg> e : suites) {
                SuiteAgg a = e.getValue();
                double suitePassRate = a.total == 0 ? 0.0 : (a.passed * 100.0 / a.total);
                suiteRows.append("<tr>")
                        .append("<td>").append(escape(e.getKey())).append("</td>")
                        .append("<td>").append(a.total).append("</td>")
                        .append("<td>").append(a.passed).append("</td>")
                        .append("<td>").append(a.failed).append("</td>")
                        .append("<td>").append(a.skipped).append("</td>")
                        .append("<td>").append(String.format("%.1f%%", suitePassRate)).append("</td>")
                        .append("</tr>");
            }
        }

        StringBuilder failureRows = new StringBuilder();
        if (failedRows.isEmpty()) {
            failureRows.append("<tr><td colspan='3'>No failed/broken tests.</td></tr>");
        } else {
            for (Row r : failedRows) {
                failureRows.append("<tr>")
                        .append("<td>").append(escape(r.name)).append("</td>")
                        .append("<td>").append(escape(r.suite)).append("</td>")
                        .append("<td>").append(escape(r.error == null || r.error.isBlank() ? "-" : r.error)).append("</td>")
                        .append("</tr>");
            }
        }

        return """
                <!doctype html>
                <html lang='en'>
                <head>
                  <meta charset='utf-8'/>
                  <meta name='viewport' content='width=device-width, initial-scale=1'/>
                  <title>Stakeholder Test Execution Summary</title>
                  <style>
                    body { font-family: Arial, sans-serif; margin: 20px; color: #1f2937; }
                    h1 { margin-bottom: 4px; }
                    .muted { color: #6b7280; margin-bottom: 16px; }
                    .cards { display: grid; grid-template-columns: repeat(6, minmax(110px, 1fr)); gap: 10px; margin: 16px 0 20px; }
                    .card { border-radius: 10px; padding: 12px; color: white; text-align: center; }
                    .card .num { font-size: 24px; font-weight: bold; }
                    .card .lbl { font-size: 12px; opacity: .95; }
                    .total { background: #111827; } .passed { background:#16a34a; } .failed { background:#dc2626; }
                    .skipped { background:#6b7280; } .rate { background:#2563eb; } .unique { background:#7c3aed; }
                    table { border-collapse: collapse; width: 100%; margin-bottom: 18px; }
                    th, td { border: 1px solid #e5e7eb; padding: 8px; font-size: 13px; vertical-align: top; }
                    th { background: #f3f4f6; text-align: left; }
                    .section { margin-top: 22px; }
                    .ok { color:#16a34a; font-weight:700; } .bad { color:#dc2626; font-weight:700; }
                  </style>
                </head>
                <body>
                """
                + "<h1>Stakeholder Test Execution Summary</h1>"
                + "<div class='muted'>Generated: " + escape(generatedAt) + "</div>"
                + "<div><b>Overall Outcome:</b> " + (failed == 0 ? "<span class='ok'>PASS</span>" : "<span class='bad'>ATTENTION NEEDED</span>") + "</div>"
                + "<div class='cards'>"
                + card("total", total, "TOTAL EXECUTIONS")
                + card("passed", passed, "PASSED")
                + card("failed", failed, "FAILED/BROKEN")
                + card("skipped", skipped, "SKIPPED")
                + card("rate", String.format("%.1f%%", passRate), "PASS RATE")
                + card("unique", uniqueTests.size(), "UNIQUE TEST CASES")
                + "</div>"
                + "<div><b>Unique failed test cases:</b> " + uniqueFailedTests.size() + "</div>"

                + "<div class='section'><h2>Suite-wise Summary</h2>"
                + "<table><thead><tr><th>Suite</th><th>Total</th><th>Passed</th><th>Failed</th><th>Skipped</th><th>Pass Rate</th></tr></thead><tbody>"
                + suiteRows
                + "</tbody></table></div>"

                + "<div class='section'><h2>Top Failure Details (max 20)</h2>"
                + "<table><thead><tr><th>Test Name</th><th>Suite</th><th>Error Message</th></tr></thead><tbody>"
                + failureRows
                + "</tbody></table></div>"

                + "</body></html>";
    }

    private static String card(String clazz, Object value, String label) {
        return "<div class='card " + clazz + "'><div class='num'>" + value + "</div><div class='lbl'>" + label + "</div></div>";
    }

    private static void patchIndex(Path reportDir) throws IOException {
        Path index = reportDir.resolve("index.html");
        Path original = reportDir.resolve("index.allure.html");
        Path portable = reportDir.resolve("portable-index.html");

        if (Files.exists(index) && !Files.exists(original)) {
            Files.move(index, original);
        }

        String shim = """
                <!doctype html>
                <html lang='en'>
                <head><meta charset='utf-8'/><title>Report Launcher</title></head>
                <body style="font-family:Arial,sans-serif;padding:20px;">
                  <h2>Automation Report Launcher</h2>
                  <p>This package contains both full Allure UI and single-file stakeholder HTML.</p>
                  <ul>
                    <li><a href='portable-index.html'>Open stakeholder one-file HTML report</a></li>
                    <li><a href='index.allure.html'>Open full interactive Allure UI (requires HTTP server)</a></li>
                  </ul>
                  <script>
                    if (location.protocol === 'file:') {
                      location.href = 'portable-index.html';
                    }
                  </script>
                </body>
                </html>
                """;

        Files.writeString(index, shim, StandardCharsets.UTF_8);
        if (!Files.exists(portable)) {
            Files.writeString(portable, "<html><body><h3>portable-index.html not generated yet.</h3></body></html>", StandardCharsets.UTF_8);
        }
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static final class SuiteAgg {
        long total;
        long passed;
        long failed;
        long skipped;
        long unknown;
    }

    private static final class Row {
        private final String name;
        private final String status;
        private final String suite;
        private final String pkg;
        private final Long durationMs;
        private final String error;

        private Row(String name, String status, String suite, String pkg, Long durationMs, String error) {
            this.name = name;
            this.status = status;
            this.suite = suite;
            this.pkg = pkg;
            this.durationMs = durationMs;
            this.error = error;
        }
    }
}
