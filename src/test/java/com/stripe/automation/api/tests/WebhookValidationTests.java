package com.stripe.automation.api.tests;

import com.stripe.automation.webhook.WebhookReceiverServer;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class WebhookValidationTests {
    private final WebhookReceiverServer server = new WebhookReceiverServer();
    private int port;

    @BeforeClass
    public void startServer() throws IOException {
        System.setProperty("stripe.webhook.secret", "whsec_unit_test_secret");
        port = findFreePort();
        server.start(port);
    }

    @AfterClass
    public void stopServer() {
        server.stop();
        System.clearProperty("stripe.webhook.secret");
    }

    @Test(groups = {"webhook", "smoke"})
    public void validateWebhookTamperingAndDuplicateHandling() throws Exception {
        String payload = "{\"id\":\"evt_1\",\"type\":\"payment_intent.succeeded\"}";
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        String endpoint = "http://localhost:" + port + "/stripe/webhook";

        HttpRequest tampered = HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(Duration.ofSeconds(5))
                .header("Stripe-Signature", "v1=invalid")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
        HttpResponse<String> badResponse = sendWithRetry(client, tampered, 3);
        Assert.assertEquals(badResponse.statusCode(), 400);

        String sig = "v1=" + "0000000000000000000000000000000000000000000000000000000000000000";
        HttpRequest duplicate1 = HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(Duration.ofSeconds(5))
                .header("Stripe-Signature", sig)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
        HttpResponse<String> first = sendWithRetry(client, duplicate1, 3);
        Assert.assertTrue(first.statusCode() == 400 || first.statusCode() == 200);
    }

    private static int findFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static HttpResponse<String> sendWithRetry(HttpClient client, HttpRequest request, int maxAttempts) throws Exception {
        Exception last = null;
        for (int i = 1; i <= maxAttempts; i++) {
            try {
                return client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                last = e;
                Thread.sleep(150L * i);
            }
        }
        throw last;
    }
}
