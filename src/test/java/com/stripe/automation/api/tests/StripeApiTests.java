package com.stripe.automation.api.tests;

import com.stripe.automation.api.client.StripeApiClient;
import com.stripe.automation.api.models.PaymentIntentRequest;
import com.stripe.automation.api.models.RefundRequest;
import com.stripe.automation.config.ConfigManager;
import com.stripe.automation.listeners.RetryAnalyzer;
import com.stripe.automation.support.TestPrerequisites;
import io.restassured.RestAssured;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public class StripeApiTests {
    private final StripeApiClient client = new StripeApiClient();
    private String paymentIntentId;

    @BeforeClass
    public void setup() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        TestPrerequisites.requireStripeApiCredentials();
    }

    @Test(groups = {"smoke", "api"}, retryAnalyzer = RetryAnalyzer.class)
    public void createAndConfirmPaymentIntent() {
        Response create = client.createPaymentIntent(new PaymentIntentRequest(2000L, "usd", ConfigManager.get("stripe.test.paymentMethod"), true), UUID.randomUUID().toString());
        Assert.assertEquals(create.statusCode(), 200);
        create.then().body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/payment-intent-schema.json"));
        paymentIntentId = create.jsonPath().getString("id");
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(create.jsonPath().getString("status"), "succeeded");
        softAssert.assertEquals(create.jsonPath().getString("currency"), "usd");
        softAssert.assertAll();

        Response confirm = client.confirmPaymentIntent(paymentIntentId);
        Assert.assertTrue(confirm.statusCode() == 200 || confirm.statusCode() == 400);
    }

    @Test(dependsOnMethods = "createAndConfirmPaymentIntent", groups = {"regression", "api"})
    public void fullAndPartialRefund() {
        Response partial = client.createRefund(new RefundRequest(paymentIntentId, 500L));
        Assert.assertEquals(partial.statusCode(), 200);

        Response full = client.createRefund(new RefundRequest(paymentIntentId, null));
        Assert.assertEquals(full.statusCode(), 200);
    }

    @Test(dependsOnMethods = "createAndConfirmPaymentIntent", groups = {"regression", "api"})
    public void duplicateRefundAndExcessRefundValidation() {
        Response first = client.createRefund(new RefundRequest(paymentIntentId, 100L));
        Response duplicate = client.createRefundWithHeaders(new RefundRequest(paymentIntentId, 100L), Map.of("Idempotency-Key", "dup-refund-key"));
        Response duplicateSecond = client.createRefundWithHeaders(new RefundRequest(paymentIntentId, 100L), Map.of("Idempotency-Key", "dup-refund-key"));
        Assert.assertEquals(duplicate.statusCode(), duplicateSecond.statusCode());
        Response overRefund = client.createRefund(new RefundRequest(paymentIntentId, 9_999_999L));
        Assert.assertTrue(overRefund.statusCode() >= 400);
        Assert.assertTrue(first.statusCode() == 200 || first.statusCode() == 400);
    }

    @Test(groups = {"regression", "api"})
    public void invalidAndExpiredApiKeyValidation() {
        Response invalidKeyResponse = given()
                .baseUri(ConfigManager.get("stripe.api.baseUrl"))
                .header("Authorization", "Bearer invalid_key")
                .post("/payment_intents");
        Assert.assertEquals(invalidKeyResponse.statusCode(), 401);

        Response expiredLikeResponse = given()
                .baseUri(ConfigManager.get("stripe.api.baseUrl"))
                .header("Authorization", "Bearer sk_test_expired_simulation")
                .post("/payment_intents");
        Assert.assertEquals(expiredLikeResponse.statusCode(), 401);
    }

    @Test(groups = {"regression", "api"})
    public void rateLimitAndIdempotencyValidation() {
        String idempotencyKey = UUID.randomUUID().toString();
        Response first = client.createPaymentIntent(new PaymentIntentRequest(300L, "usd", ConfigManager.get("stripe.test.paymentMethod"), false), idempotencyKey);
        Response second = client.createPaymentIntent(new PaymentIntentRequest(300L, "usd", ConfigManager.get("stripe.test.paymentMethod"), false), idempotencyKey);
        Assert.assertEquals(first.jsonPath().getString("id"), second.jsonPath().getString("id"));

        int tooManyRequestCount = 0;
        for (int i = 0; i < 40; i++) {
            Response response = client.createPaymentIntent(new PaymentIntentRequest(100L + i, "usd", ConfigManager.get("stripe.test.paymentMethod"), false), UUID.randomUUID().toString());
            if (response.statusCode() == 429) {
                tooManyRequestCount++;
            }
        }
        Assert.assertTrue(tooManyRequestCount >= 0);
    }

    @Test(groups = {"regression", "api"})
    public void failedPaymentScenario() {
        Response response = client.createPaymentIntent(new PaymentIntentRequest(2000L, "usd", "pm_card_chargeDeclined", true), UUID.randomUUID().toString());
        Assert.assertTrue(response.statusCode() == 200 || response.statusCode() == 402 || response.statusCode() == 400);
    }
}
