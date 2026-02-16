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
import java.util.List;
import java.util.Map;
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

        System.out.println("Portable report generated: " + output);
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
                    rows.add(new Row(name, status, suite, pkg, durationMs));
                } catch (Exception ignored) {
                }
            });
        }

        rows.sort(Comparator.comparingInt(r -> {
            int i = STATUS_ORDER.indexOf(r.status);
            return i >= 0 ? i : 99;
        }).thenComparing(r -> r.name));

        return rows;
    }

    private static String buildHtml(List<Row> rows, String generatedAt) {
        Map<String, Long> totals = Map.of(
                "passed", rows.stream().filter(r -> "passed".equals(r.status)).count(),
                "failed", rows.stream().filter(r -> "failed".equals(r.status)).count(),
                "broken", rows.stream().filter(r -> "broken".equals(r.status)).count(),
                "skipped", rows.stream().filter(r -> "skipped".equals(r.status)).count(),
                "unknown", rows.stream().filter(r -> !STATUS_ORDER.contains(r.status) || "unknown".equals(r.status)).count()
        );

        StringBuilder tableRows = new StringBuilder();
        if (rows.isEmpty()) {
            tableRows.append("<tr><td colspan='5'>No Allure result files found.</td></tr>");
        } else {
            for (Row r : rows) {
                tableRows.append("<tr>")
                        .append("<td>").append(escape(r.name)).append("</td>")
                        .append("<td><span class='badge ").append(escape(r.status)).append("'>").append(escape(r.status)).append("</span></td>")
                        .append("<td>").append(escape(r.suite)).append("</td>")
                        .append("<td>").append(escape(r.pkg)).append("</td>")
                        .append("<td>").append(r.durationMs == null ? "-" : r.durationMs + " ms").append("</td>")
                        .append("</tr>");
            }
        }

        return """
                <!doctype html>
                <html lang='en'>
                <head>
                  <meta charset='utf-8'/>
                  <meta name='viewport' content='width=device-width, initial-scale=1'/>
                  <title>Portable Automation Report</title>
                  <style>
                    body { font-family: Arial, sans-serif; margin: 20px; color: #1f2937; }
                    h1 { margin-bottom: 4px; }
                    .muted { color: #6b7280; margin-bottom: 16px; }
                    .cards { display: grid; grid-template-columns: repeat(6, minmax(110px, 1fr)); gap: 10px; margin: 16px 0 20px; }
                    .card { border-radius: 10px; padding: 12px; color: white; text-align: center; }
                    .card .num { font-size: 24px; font-weight: bold; }
                    .card .lbl { font-size: 12px; opacity: .95; }
                    .total { background: #111827; } .passed { background:#16a34a; } .failed { background:#dc2626; }
                    .broken { background:#f97316; } .skipped { background:#6b7280; } .unknown { background:#2563eb; }
                    table { border-collapse: collapse; width: 100%; }
                    th, td { border: 1px solid #e5e7eb; padding: 8px; font-size: 13px; }
                    th { background: #f3f4f6; text-align: left; }
                    .badge { padding: 2px 8px; border-radius: 999px; color: white; font-size: 12px; }
                    .badge.passed { background:#16a34a; } .badge.failed { background:#dc2626; }
                    .badge.broken { background:#f97316; } .badge.skipped { background:#6b7280; } .badge.unknown { background:#2563eb; }
                  </style>
                </head>
                <body>
                  <h1>Portable Execution Report</h1>
                """
                + "<div class='muted'>Generated: " + escape(generatedAt) + " • Total cases: " + rows.size() + "</div>"
                + "<div class='cards'>"
                + "<div class='card total'><div class='num'>" + rows.size() + "</div><div class='lbl'>TOTAL</div></div>"
                + card("passed", totals.get("passed"))
                + card("failed", totals.get("failed"))
                + card("broken", totals.get("broken"))
                + card("skipped", totals.get("skipped"))
                + card("unknown", totals.get("unknown"))
                + "</div>"
                + "<h2>Test Details</h2><table><thead><tr><th>Name</th><th>Status</th><th>Suite</th><th>Package</th><th>Duration</th></tr></thead><tbody>"
                + tableRows
                + "</tbody></table></body></html>";
    }

    private static String card(String status, Long count) {
        return "<div class='card " + status + "'><div class='num'>" + count + "</div><div class='lbl'>" + status.toUpperCase() + "</div></div>";
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
                <head><meta charset='utf-8'/><title>Allure Report Launcher</title></head>
                <body style="font-family:Arial,sans-serif;padding:20px;">
                  <h2>Automation Report Launcher</h2>
                  <p>When opened from <code>file://</code>, this page redirects to a portable offline report.</p>
                  <ul>
                    <li><a href='portable-index.html'>Open portable report (offline/email friendly)</a></li>
                    <li><a href='index.allure.html'>Open full Allure UI (requires HTTP server, not file://)</a></li>
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

    private static final class Row {
        private final String name;
        private final String status;
        private final String suite;
        private final String pkg;
        private final Long durationMs;

        private Row(String name, String status, String suite, String pkg, Long durationMs) {
            this.name = name;
            this.status = status;
            this.suite = suite;
            this.pkg = pkg;
            this.durationMs = durationMs;
        }
    }
}
