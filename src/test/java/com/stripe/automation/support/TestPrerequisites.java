package com.stripe.automation.support;

import com.stripe.automation.config.ConfigManager;
import org.testng.SkipException;

public final class TestPrerequisites {
    private TestPrerequisites() {}

    public static void requireStripeApiCredentials() {
        String secretKey = ConfigManager.get("stripe.secretKey");
        if (secretKey == null || secretKey.isBlank()) {
            throw new SkipException("Skipping Stripe API tests: stripe.secretKey / STRIPE_SECRET_KEY is not configured");
        }
    }

    public static void requireUiCredentials() {
        String url = ConfigManager.get("stripe.dashboard.url");
        String email = ConfigManager.get("stripe.dashboard.email");
        String password = ConfigManager.get("stripe.dashboard.password");

        if (url == null || url.isBlank() || email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new SkipException("Skipping Stripe UI tests: dashboard url/email/password are not configured");
        }
    }
}
