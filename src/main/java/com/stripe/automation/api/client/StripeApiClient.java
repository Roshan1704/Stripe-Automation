package com.stripe.automation.api.client;

import com.stripe.automation.api.models.PaymentIntentRequest;
import com.stripe.automation.api.models.RefundRequest;
import com.stripe.automation.api.spec.ApiSpecifications;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class StripeApiClient {

    public Response createPaymentIntent(PaymentIntentRequest request, String idempotencyKey) {
        return given()
                .spec(ApiSpecifications.stripeRequestSpec())
                .header("Idempotency-Key", idempotencyKey)
                .formParam("amount", request.amount())
                .formParam("currency", request.currency())
                .formParam("payment_method", request.payment_method())
                .formParam("confirm", request.confirm())
                .post("/payment_intents");
    }

    public Response confirmPaymentIntent(String paymentIntentId) {
        return given()
                .spec(ApiSpecifications.stripeRequestSpec())
                .post("/payment_intents/{id}/confirm", paymentIntentId);
    }

    public Response createRefund(RefundRequest request) {
        var req = given()
                .spec(ApiSpecifications.stripeRequestSpec())
                .formParam("payment_intent", request.payment_intent());
        if (request.amount() != null) {
            req.formParam("amount", request.amount());
        }
        return req.post("/refunds");
    }

    public Response createRefundWithHeaders(RefundRequest request, Map<String, String> headers) {
        var req = given().spec(ApiSpecifications.stripeRequestSpec()).headers(headers)
                .formParam("payment_intent", request.payment_intent());
        if (request.amount() != null) {
            req.formParam("amount", request.amount());
        }
        return req.post("/refunds");
    }
}
