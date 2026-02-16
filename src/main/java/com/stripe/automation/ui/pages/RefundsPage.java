package com.stripe.automation.ui.pages;

import com.stripe.automation.base.BaseUiPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class RefundsPage extends BaseUiPage {
    private final By header = By.xpath("//h1[contains(.,'Refunds')]");
    private final By statusFilter = By.xpath("//button[contains(.,'Status') or contains(.,'Filter')]");

    public RefundsPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return waitFor(header).isDisplayed();
    }

    public boolean hasFilterControls() {
        return waitFor(statusFilter).isDisplayed();
    }
}
