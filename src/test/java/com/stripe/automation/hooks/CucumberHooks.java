package com.stripe.automation.hooks;

import com.stripe.automation.drivers.WebDriverFactory;
import io.cucumber.java.After;
import io.cucumber.java.Before;

public class CucumberHooks {

    @Before("@ui")
    public void beforeScenario() {
        // Initialize browser session only. Actual login/navigation happens in step definitions.
        WebDriverFactory.getDriver();
    }

    @After("@ui")
    public void afterScenario() {
        WebDriverFactory.quitDriver();
    }
}
