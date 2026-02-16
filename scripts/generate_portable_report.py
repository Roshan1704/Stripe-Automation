#!/usr/bin/env python3
"""
Generate a standalone HTML report from Allure result JSON files.
Optionally patch Allure's index.html so direct file:// opening works.
"""
from __future__ import annotations

import argparse
import datetime as dt
import html
import json
from pathlib import Path
from typing import Any


STATUS_ORDER = ["failed", "broken", "skipped", "passed", "unknown"]


def load_results(results_dir: Path) -> list[dict[str, Any]]:
    rows: list[dict[str, Any]] = []
    for file in sorted(results_dir.glob("*-result.json")):
        try:
            data = json.loads(file.read_text(encoding="utf-8"))
        except Exception:
            continue

        name = data.get("name", file.stem)
        status = data.get("status", "unknown")
        start = data.get("start")
        stop = data.get("stop")
        duration_ms = (stop - start) if isinstance(start, int) and isinstance(stop, int) else None
        labels = {l.get("name"): l.get("value") for l in data.get("labels", []) if isinstance(l, dict)}

        rows.append(
            {
                "name": str(name),
                "status": str(status),
                "suite": labels.get("suite", labels.get("parentSuite", "-")),
                "package": labels.get("package", "-"),
                "duration_ms": duration_ms,
            }
        )
    return rows


def build_html(rows: list[dict[str, Any]], generated_at: str) -> str:
    totals = {k: 0 for k in STATUS_ORDER}
    for row in rows:
        totals[row["status"] if row["status"] in totals else "unknown"] += 1

    total = len(rows)

    def card(status: str) -> str:
        return (
            f"<div class='card {status}'><div class='num'>{totals[status]}</div>"
            f"<div class='lbl'>{status.upper()}</div></div>"
        )

    rows_sorted = sorted(rows, key=lambda r: (STATUS_ORDER.index(r["status"]) if r["status"] in STATUS_ORDER else 99, r["name"]))

    body_rows = []
    for r in rows_sorted:
        dur = "-" if r["duration_ms"] is None else f"{r['duration_ms']} ms"
        body_rows.append(
            "<tr>"
            f"<td>{html.escape(r['name'])}</td>"
            f"<td><span class='badge {html.escape(r['status'])}'>{html.escape(r['status'])}</span></td>"
            f"<td>{html.escape(str(r['suite']))}</td>"
            f"<td>{html.escape(str(r['package']))}</td>"
            f"<td>{dur}</td>"
            "</tr>"
        )

    return f"""<!doctype html>
<html lang='en'>
<head>
  <meta charset='utf-8'/>
  <meta name='viewport' content='width=device-width, initial-scale=1'/>
  <title>Portable Automation Report</title>
  <style>
    body {{ font-family: Arial, sans-serif; margin: 20px; color: #1f2937; }}
    h1 {{ margin-bottom: 4px; }}
    .muted {{ color: #6b7280; margin-bottom: 16px; }}
    .cards {{ display: grid; grid-template-columns: repeat(6, minmax(110px, 1fr)); gap: 10px; margin: 16px 0 20px; }}
    .card {{ border-radius: 10px; padding: 12px; color: white; text-align: center; }}
    .card .num {{ font-size: 24px; font-weight: bold; }}
    .card .lbl {{ font-size: 12px; opacity: .95; }}
    .total {{ background: #111827; }} .passed {{ background:#16a34a; }} .failed {{ background:#dc2626; }}
    .broken {{ background:#f97316; }} .skipped {{ background:#6b7280; }} .unknown {{ background:#2563eb; }}
    table {{ border-collapse: collapse; width: 100%; }}
    th, td {{ border: 1px solid #e5e7eb; padding: 8px; font-size: 13px; }}
    th {{ background: #f3f4f6; text-align: left; }}
    .badge {{ padding: 2px 8px; border-radius: 999px; color: white; font-size: 12px; }}
    .badge.passed {{ background:#16a34a; }} .badge.failed {{ background:#dc2626; }}
    .badge.broken {{ background:#f97316; }} .badge.skipped {{ background:#6b7280; }} .badge.unknown {{ background:#2563eb; }}
  </style>
</head>
<body>
  <h1>Portable Execution Report</h1>
  <div class='muted'>Generated: {html.escape(generated_at)} • Total cases: {total}</div>
  <div class='cards'>
    <div class='card total'><div class='num'>{total}</div><div class='lbl'>TOTAL</div></div>
    {card('passed')}
    {card('failed')}
    {card('broken')}
    {card('skipped')}
    {card('unknown')}
  </div>

  <h2>Test Details</h2>
  <table>
    <thead><tr><th>Name</th><th>Status</th><th>Suite</th><th>Package</th><th>Duration</th></tr></thead>
    <tbody>
      {''.join(body_rows) if body_rows else '<tr><td colspan="5">No Allure result files found.</td></tr>'}
    </tbody>
  </table>
</body>
</html>
"""


def patch_allure_index(allure_report_dir: Path) -> None:
    index = allure_report_dir / "index.html"
    original = allure_report_dir / "index.allure.html"
    portable = allure_report_dir / "portable-index.html"

    if index.exists() and not original.exists():
        index.rename(original)

    shim = """<!doctype html>
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
"""
    index.write_text(shim, encoding="utf-8")

    if not portable.exists():
        # caller is expected to generate this file; keep fallback message
        portable.write_text("<html><body><h3>portable-index.html not generated yet.</h3></body></html>", encoding="utf-8")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--results", default="target/allure-results", help="Allure results directory")
    parser.add_argument("--out", default="target/site/allure-maven-plugin/portable-index.html", help="Output standalone HTML")
    parser.add_argument("--patch-index", action="store_true", help="Patch allure index.html for file:// compatibility")
    args = parser.parse_args()

    results_dir = Path(args.results)
    out = Path(args.out)
    out.parent.mkdir(parents=True, exist_ok=True)

    rows = load_results(results_dir)
    generated_at = dt.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    out.write_text(build_html(rows, generated_at), encoding="utf-8")

    if args.patch_index:
        patch_allure_index(out.parent)

    print(f"Portable report generated: {out}")


if __name__ == "__main__":
    main()
