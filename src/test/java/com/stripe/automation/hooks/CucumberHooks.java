package com.stripe.automation.hooks;

import com.stripe.automation.config.ConfigManager;
import com.stripe.automation.drivers.WebDriverFactory;
import io.cucumber.java.After;
import io.cucumber.java.Before;

public class CucumberHooks {

    @Before("@ui")
    public void beforeScenario() {
        WebDriverFactory.getDriver().get(ConfigManager.get("stripe.dashboard.url"));
    }

    @After("@ui")
    public void afterScenario() {
        WebDriverFactory.quitDriver();
    }
}
