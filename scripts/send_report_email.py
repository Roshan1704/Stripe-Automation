#!/usr/bin/env python3
"""
Send portable HTML report (and optional full Allure zip) by email.
"""
from __future__ import annotations

import argparse
import os
import smtplib
import ssl
from email.message import EmailMessage
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED


def zip_folder(src_dir: Path, zip_path: Path) -> Path:
    with ZipFile(zip_path, "w", ZIP_DEFLATED) as zf:
        for file in src_dir.rglob("*"):
            if file.is_file():
                zf.write(file, arcname=file.relative_to(src_dir))
    return zip_path


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--smtp-host", default=os.getenv("SMTP_HOST"), required=os.getenv("SMTP_HOST") is None)
    parser.add_argument("--smtp-port", type=int, default=int(os.getenv("SMTP_PORT", "587")))
    parser.add_argument("--smtp-user", default=os.getenv("SMTP_USER"), required=os.getenv("SMTP_USER") is None)
    parser.add_argument("--smtp-pass", default=os.getenv("SMTP_PASS"), required=os.getenv("SMTP_PASS") is None)
    parser.add_argument("--from", dest="from_addr", default=os.getenv("REPORT_FROM", os.getenv("SMTP_USER")))
    parser.add_argument("--to", dest="to_addr", default=os.getenv("REPORT_TO"), required=os.getenv("REPORT_TO") is None)
    parser.add_argument("--subject", default="Stripe Automation Execution Report")
    parser.add_argument("--portable-report", default="target/site/allure-maven-plugin/portable-index.html")
    parser.add_argument("--allure-dir", default="target/site/allure-maven-plugin")
    parser.add_argument("--attach-allure-zip", action="store_true")
    args = parser.parse_args()

    portable = Path(args.portable_report)
    if not portable.exists():
        raise FileNotFoundError(f"Portable report not found: {portable}")

    msg = EmailMessage()
    msg["Subject"] = args.subject
    msg["From"] = args.from_addr
    msg["To"] = args.to_addr
    msg.set_content(
        "Please find attached automation reports.\n"
        "- portable-report.html can be opened directly from email/download (no server required).\n"
        "- full-allure-report.zip contains complete interactive Allure report (serve via HTTP)."
    )

    portable_bytes = portable.read_bytes()
    msg.add_attachment(portable_bytes, maintype="text", subtype="html", filename="portable-report.html")

    if args.attach_allure_zip:
        allure_dir = Path(args.allure_dir)
        if allure_dir.exists():
            zip_path = allure_dir.parent / "full-allure-report.zip"
            zip_folder(allure_dir, zip_path)
            msg.add_attachment(zip_path.read_bytes(), maintype="application", subtype="zip", filename=zip_path.name)

    context = ssl.create_default_context()
    with smtplib.SMTP(args.smtp_host, args.smtp_port) as server:
        server.starttls(context=context)
        server.login(args.smtp_user, args.smtp_pass)
        server.send_message(msg)

    print("Report email sent successfully")


if __name__ == "__main__":
    main()
