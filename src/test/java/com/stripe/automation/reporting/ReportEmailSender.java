package com.stripe.automation.reporting;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.File;
import java.nio.file.Path;
import java.util.Properties;

public final class ReportEmailSender {
    private ReportEmailSender() {
    }

    public static void main(String[] args) throws Exception {
        String smtpHost = required("SMTP_HOST");
        String smtpPort = configOrDefault("SMTP_PORT", "587");
        String smtpUser = required("SMTP_USER");
        String smtpPass = required("SMTP_PASS");

        String from = configOrDefault("REPORT_FROM", smtpUser);
        String to = required("REPORT_TO");
        String subject = configOrDefault("REPORT_SUBJECT", "Stripe Automation Execution Report");

        Path portableReport = Path.of(configOrDefault("PORTABLE_REPORT_PATH", "target/site/allure-maven-plugin/portable-index.html"));
        ensurePortableReportExists(portableReport);

        boolean attachAllureZip = Boolean.parseBoolean(configOrDefault("ATTACH_ALLURE_ZIP", "true"));
        Path allureZip = Path.of(configOrDefault("ALLURE_ZIP_PATH", "target/site/full-allure-report.zip"));

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUser, smtpPass);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);

        MimeBodyPart text = new MimeBodyPart();
        text.setText(
                "Please find attached automation reports.\n"
                        + "- portable-report.html can be opened directly from email/download (no server required).\n"
                        + "- full-allure-report.zip contains complete interactive Allure report for HTTP-hosted viewing."
        );

        MimeBodyPart portableAttachment = new MimeBodyPart();
        portableAttachment.attachFile(portableReport.toFile());
        portableAttachment.setFileName("portable-report.html");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(text);
        multipart.addBodyPart(portableAttachment);

        if (attachAllureZip) {
            File zipFile = allureZip.toFile();
            if (zipFile.exists()) {
                MimeBodyPart zipAttachment = new MimeBodyPart();
                zipAttachment.attachFile(zipFile);
                zipAttachment.setFileName("full-allure-report.zip");
                multipart.addBodyPart(zipAttachment);
            }
        }

        message.setContent(multipart);
        Transport.send(message);
        System.out.println("Report email sent successfully");
    }

    private static void ensurePortableReportExists(Path portableReport) throws Exception {
        if (portableReport.toFile().exists()) {
            return;
        }

        System.out.println("Portable report not found at " + portableReport + ". Attempting to generate it now...");
        if (System.getProperty("PORTABLE_REPORT_PATH") == null) {
            System.setProperty("PORTABLE_REPORT_PATH", portableReport.toString());
        }
        if (System.getProperty("PATCH_ALLURE_INDEX") == null) {
            System.setProperty("PATCH_ALLURE_INDEX", "true");
        }
        PortableReportGenerator.main(new String[0]);

        if (!portableReport.toFile().exists()) {
            throw new IllegalStateException(
                    "Portable report not found and auto-generation failed: " + portableReport
                            + " (run mvn allure:report, then exec:java@generate-portable-report)"
            );
        }
    }

    private static String required(String key) {
        String value = config(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Required SMTP config missing: " + key
                            + " (set env var " + key + " or JVM property -D" + key + ")"
            );
        }
        return value;
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
}
