package com.stripe.automation.ui.pages;

import com.stripe.automation.base.BaseUiPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class DisputesPage extends BaseUiPage {
    private final By header = By.xpath("//h1[contains(.,'Disputes')]");

    public DisputesPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return waitFor(header).isDisplayed();
    }
}
