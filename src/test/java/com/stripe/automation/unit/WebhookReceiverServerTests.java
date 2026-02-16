package com.stripe.automation.unit;

import com.stripe.automation.webhook.WebhookReceiverServer;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class WebhookReceiverServerTests {
    private final WebhookReceiverServer server = new WebhookReceiverServer();

    @BeforeClass
    public void setUp() throws Exception {
        System.setProperty("stripe.webhook.secret", "unit-test-secret");
        server.start(9091);
    }

    @AfterClass
    public void tearDown() {
        server.stop();
        System.clearProperty("stripe.webhook.secret");
    }

    @Test
    public void shouldRejectMissingSignatureHeader() {
        Assert.assertFalse(server.verifySignature("{}", null));
    }

    @Test
    public void shouldValidateCorrectSignature() {
        String payload = "{\"id\":\"evt_123\",\"type\":\"payment_intent.succeeded\"}";
        String sig = "v1=" + hmac("unit-test-secret", payload);
        Assert.assertTrue(server.verifySignature(payload, sig));
    }

    @Test
    public void shouldRejectTamperedSignature() {
        String payload = "{\"id\":\"evt_124\",\"type\":\"payment_intent.failed\"}";
        Assert.assertFalse(server.verifySignature(payload, "v1=invalid-signature"));
    }

    @Test
    public void shouldMarkDuplicateEventsAsAlreadyReported() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String payload = "{\"id\":\"evt_duplicate\",\"type\":\"payment_intent.succeeded\"}";
        String sig = "v1=" + hmac("unit-test-secret", payload);

        HttpRequest firstRequest = HttpRequest.newBuilder(URI.create("http://localhost:9091/stripe/webhook"))
                .header("Stripe-Signature", sig)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
        HttpResponse<String> firstResponse = client.send(firstRequest, HttpResponse.BodyHandlers.ofString());

        HttpRequest secondRequest = HttpRequest.newBuilder(URI.create("http://localhost:9091/stripe/webhook"))
                .header("Stripe-Signature", sig)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
        HttpResponse<String> secondResponse = client.send(secondRequest, HttpResponse.BodyHandlers.ofString());

        Assert.assertEquals(firstResponse.statusCode(), 200);
        Assert.assertEquals(secondResponse.statusCode(), 208);
    }

    private String hmac(String secret, String payload) {
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
}
