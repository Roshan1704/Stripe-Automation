package com.stripe.automation.unit;

import com.stripe.automation.config.ConfigManager;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class ConfigManagerTests {

    @AfterMethod
    public void clearOverrides() {
        System.clearProperty("stripe.api.baseUrl");
        System.clearProperty("missing.key");
    }

    @Test
    public void shouldReturnValueFromLoadedProperties() {
        Assert.assertEquals(ConfigManager.get("webhook.port"), "9090");
    }

    @Test
    public void shouldPrioritizeSystemPropertyOverConfig() {
        System.setProperty("stripe.api.baseUrl", "https://example.test");
        Assert.assertEquals(ConfigManager.get("stripe.api.baseUrl"), "https://example.test");
    }

    @Test
    public void shouldReturnNullForUnknownKey() {
        Assert.assertNull(ConfigManager.get("missing.key"));
    }
}
