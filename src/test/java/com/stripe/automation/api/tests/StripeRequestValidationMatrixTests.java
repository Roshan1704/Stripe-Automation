package com.stripe.automation.api.tests;

import com.stripe.automation.api.models.PaymentIntentRequest;
import com.stripe.automation.api.validation.StripeRequestValidator;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class StripeRequestValidationMatrixTests {

    @DataProvider(name = "paymentIntentValidationCases")
    public Object[][] paymentIntentValidationCases() {
        List<Object[]> rows = new ArrayList<>();
        long[] amounts = {0L, 10L, 49L, 50L, 100L, 500L, 10_000L, 99_999_999L, 100_000_000L, -1L};
        String[] currencies = {"usd", "eur", "gbp", "aud", "cad", "inr", "", "USD"};
        String[] methods = {"pm_card_visa", "pm_card_mastercard", "pm_card_chargeDeclined", "", "   "};

        for (long amount : amounts) {
            for (String currency : currencies) {
                for (String method : methods) {
                    boolean validAmount = amount >= 50 && amount <= 99_999_999;
                    boolean validCurrency = currency != null && !currency.isBlank()
                            && List.of("usd", "eur", "gbp", "aud", "cad").contains(currency.toLowerCase());
                    boolean validMethod = method != null && !method.isBlank();
                    boolean expectedValid = validAmount && validCurrency && validMethod;
                    rows.add(new Object[]{new PaymentIntentRequest(amount, currency, method, true), expectedValid});
                }
            }
        }

        if (rows.size() < 300) {
            throw new IllegalStateException("Expected at least 300 test cases but got " + rows.size());
        }

        return rows.toArray(new Object[0][]);
    }

    @Test(dataProvider = "paymentIntentValidationCases", groups = {"api", "unit", "regression"})
    public void tcApiValidationMatrix_shouldValidatePaymentIntentRequests(PaymentIntentRequest request, boolean expectedValid) {
        if (expectedValid) {
            StripeRequestValidator.validatePaymentIntent(request);
            return;
        }

        try {
            StripeRequestValidator.validatePaymentIntent(request);
            Assert.fail("Expected validation to fail for: " + request);
        } catch (IllegalArgumentException expected) {
            Assert.assertTrue(expected.getMessage() != null && !expected.getMessage().isBlank());
        }
    }
}
