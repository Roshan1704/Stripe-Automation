package com.stripe.automation.ui.pages;

import com.stripe.automation.base.BaseUiPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CustomersPage extends BaseUiPage {
    private final By header = By.xpath("//h1[contains(.,'Customers')]");
    private final By search = By.cssSelector("input[placeholder*='Search']");

    public CustomersPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return waitFor(header).isDisplayed();
    }

    public void searchCustomer(String emailOrId) {
        waitFor(search).clear();
        waitFor(search).sendKeys(emailOrId);
    }
}
