package com.stripe.automation.listeners;

import com.stripe.automation.drivers.WebDriverFactory;
import com.stripe.automation.utils.ScreenshotUtils;
import io.qameta.allure.Allure;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.IOException;
import java.nio.file.Files;

public class TestListener implements ITestListener {
    @Override
    public void onTestFailure(ITestResult result) {
        WebDriver driver = WebDriverFactory.getDriver();
        if (driver != null) {
            try {
                var shot = ScreenshotUtils.capture(driver, result.getMethod().getMethodName());
                Allure.addAttachment("failure-screenshot", Files.newInputStream(shot));
            } catch (IOException | RuntimeException ignored) {
            }
        }
    }

    @Override
    public void onStart(ITestContext context) {
        context.setAttribute("suiteStart", System.currentTimeMillis());
    }
}
