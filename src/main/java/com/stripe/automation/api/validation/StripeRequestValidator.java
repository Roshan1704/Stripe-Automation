package com.stripe.automation.api.validation;

import com.stripe.automation.api.models.PaymentIntentRequest;
import com.stripe.automation.api.models.RefundRequest;

import java.util.Set;

public final class StripeRequestValidator {
    private static final Set<String> SUPPORTED_CURRENCIES = Set.of("usd", "eur", "gbp", "aud", "cad");

    private StripeRequestValidator() {}

    public static void validatePaymentIntent(PaymentIntentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("PaymentIntentRequest must not be null");
        }
        if (request.amount() == null || request.amount() < 50 || request.amount() > 99_999_999) {
            throw new IllegalArgumentException("Amount must be between 50 and 99,999,999");
        }
        if (request.currency() == null || !SUPPORTED_CURRENCIES.contains(request.currency().toLowerCase())) {
            throw new IllegalArgumentException("Unsupported currency");
        }
        if (request.payment_method() == null || request.payment_method().isBlank()) {
            throw new IllegalArgumentException("payment_method must not be blank");
        }
    }

    public static void validateRefund(RefundRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("RefundRequest must not be null");
        }
        if (request.payment_intent() == null || request.payment_intent().isBlank()) {
            throw new IllegalArgumentException("payment_intent must not be blank");
        }
        if (request.amount() != null && request.amount() < 1) {
            throw new IllegalArgumentException("Refund amount must be positive");
        }
    }
}
