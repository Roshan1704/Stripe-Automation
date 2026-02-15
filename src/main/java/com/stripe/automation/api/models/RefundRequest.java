package com.stripe.automation.api.models;

public record RefundRequest(String payment_intent, Long amount) {
}
