package com.stripe.automation.ui.pages;

import com.stripe.automation.base.BaseUiPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class PaymentsPage extends BaseUiPage {
    private final By paymentsTab = By.xpath("//a[contains(@href,'payments')]");
    private final By pageHeader = By.xpath("//h1[contains(.,'Payments')]");
    private final By searchInput = By.cssSelector("input[placeholder*='Search']");
    private final By filterButton = By.xpath("//button[contains(.,'Filter')]");
    private final By pagination = By.cssSelector("nav[aria-label='Pagination']");

    public PaymentsPage(WebDriver driver) {
        super(driver);
    }

    public void openPayments() {
        waitFor(paymentsTab).click();
    }

    public boolean isLoaded() {
        return waitFor(pageHeader).isDisplayed();
    }

    public void searchPayment(String paymentId) {
        waitFor(searchInput).clear();
        waitFor(searchInput).sendKeys(paymentId);
    }

    public boolean isRefundStatusVisible(String status) {
        return waitFor(By.xpath("//*[contains(text(),'" + status + "')]")) != null;
    }

    public boolean filtersAndPaginationAvailable() {
        return waitFor(filterButton).isDisplayed() && waitFor(pagination).isDisplayed();
    }
}
