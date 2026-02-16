package com.stripe.automation.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.stripe.automation.ui.stepdefinitions", "com.stripe.automation.hooks"},
        plugin = {"pretty", "summary", "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"},
        tags = "(@smoke or @regression) and not @ui"
)
public class CucumberUiTestRunner extends AbstractTestNGCucumberTests {
    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
