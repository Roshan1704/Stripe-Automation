package com.stripe.automation.api.models;

public record PaymentIntentRequest(Long amount, String currency, String payment_method, Boolean confirm) {
}
