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
        String smtpPort = System.getenv().getOrDefault("SMTP_PORT", "587");
        String smtpUser = required("SMTP_USER");
        String smtpPass = required("SMTP_PASS");

        String from = System.getenv().getOrDefault("REPORT_FROM", smtpUser);
        String to = required("REPORT_TO");
        String subject = System.getenv().getOrDefault("REPORT_SUBJECT", "Stripe Automation Execution Report");

        Path portableReport = Path.of(System.getenv().getOrDefault("PORTABLE_REPORT_PATH", "target/site/allure-maven-plugin/portable-index.html"));
        if (!portableReport.toFile().exists()) {
            throw new IllegalStateException("Portable report not found: " + portableReport);
        }

        boolean attachAllureZip = Boolean.parseBoolean(System.getenv().getOrDefault("ATTACH_ALLURE_ZIP", "true"));
        Path allureZip = Path.of(System.getenv().getOrDefault("ALLURE_ZIP_PATH", "target/site/full-allure-report.zip"));

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

    private static String required(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required environment variable is missing: " + key);
        }
        return value;
    }
}
