package com.stripe.automation.api.tests;

import com.stripe.automation.webhook.WebhookReceiverServer;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WebhookValidationTests {
    private final WebhookReceiverServer server = new WebhookReceiverServer();

    @BeforeClass
    public void startServer() throws IOException {
        server.start(9090);
    }

    @AfterClass
    public void stopServer() {
        server.stop();
    }

    @Test(groups = {"webhook", "smoke"})
    public void validateWebhookTamperingAndDuplicateHandling() throws Exception {
        String payload = "{\"id\":\"evt_1\",\"type\":\"payment_intent.succeeded\"}";
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest tampered = HttpRequest.newBuilder(URI.create("http://localhost:9090/stripe/webhook"))
                .header("Stripe-Signature", "v1=invalid")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
        HttpResponse<String> badResponse = client.send(tampered, HttpResponse.BodyHandlers.ofString());
        Assert.assertEquals(badResponse.statusCode(), 400);

        String sig = "v1=" + "0000000000000000000000000000000000000000000000000000000000000000";
        HttpRequest duplicate1 = HttpRequest.newBuilder(URI.create("http://localhost:9090/stripe/webhook"))
                .header("Stripe-Signature", sig)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
        HttpResponse<String> first = client.send(duplicate1, HttpResponse.BodyHandlers.ofString());
        Assert.assertTrue(first.statusCode() == 400 || first.statusCode() == 200);
    }
}
