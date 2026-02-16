package com.stripe.automation.ui.pages;

import com.stripe.automation.base.BaseUiPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class DashboardHomePage extends BaseUiPage {
    private final By homeHeader = By.xpath("//h1[contains(.,'Dashboard') or contains(.,'Home')]");
    private final By paymentsNav = By.xpath("//a[contains(@href,'payments')]");
    private final By customersNav = By.xpath("//a[contains(@href,'customers')]");
    private final By refundsNav = By.xpath("//a[contains(@href,'refunds')]");
    private final By disputesNav = By.xpath("//a[contains(@href,'disputes')]");

    public DashboardHomePage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return waitFor(homeHeader).isDisplayed();
    }

    public void openPayments() {
        waitFor(paymentsNav).click();
    }

    public void openCustomers() {
        waitFor(customersNav).click();
    }

    public void openRefunds() {
        waitFor(refundsNav).click();
    }

    public void openDisputes() {
        waitFor(disputesNav).click();
    }
}
