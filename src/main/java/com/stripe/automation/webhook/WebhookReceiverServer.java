package com.stripe.automation.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.automation.config.ConfigManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebhookReceiverServer {
    private final Set<String> eventIds = ConcurrentHashMap.newKeySet();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private HttpServer server;

    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/stripe/webhook", this::handleWebhook);
        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private void handleWebhook(HttpExchange exchange) throws IOException {
        String payload = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String signature = exchange.getRequestHeaders().getFirst("Stripe-Signature");
        int status = verifySignature(payload, signature) ? 200 : 400;

        if (status == 200) {
            JsonNode node = objectMapper.readTree(payload);
            String eventId = node.path("id").asText();
            if (!eventIds.add(eventId)) {
                status = 208;
            }
        }

        exchange.sendResponseHeaders(status, 0);
        exchange.getResponseBody().write(("status=" + status).getBytes(StandardCharsets.UTF_8));
        exchange.close();
    }

    public boolean verifySignature(String payload, String stripeSigHeader) {
        if (stripeSigHeader == null || !stripeSigHeader.contains("v1=")) {
            return false;
        }
        String actual = stripeSigHeader.substring(stripeSigHeader.indexOf("v1=") + 3);
        String expected = hmacSha256(ConfigManager.get("stripe.webhook.secret"), payload);
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
    }

    private String hmacSha256(String secret, String payload) {
        try {
            Mac sha256 = Mac.getInstance("HmacSHA256");
            sha256.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = sha256.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        WebhookReceiverServer server = new WebhookReceiverServer();
        server.start(Integer.parseInt(ConfigManager.get("webhook.port")));
    }
}
