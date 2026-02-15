package com.stripe.automation.base;

import com.stripe.automation.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class BaseUiPage {
    protected final WebDriver driver;

    protected BaseUiPage(WebDriver driver) {
        this.driver = driver;
    }

    protected WebElement waitFor(By locator) {
        return WaitUtils.visible(driver, locator, 20);
    }
}
