package com.stripe.automation.ui.stepdefinitions;

import com.stripe.automation.config.ConfigManager;
import com.stripe.automation.drivers.WebDriverFactory;
import com.stripe.automation.ui.pages.LoginPage;
import com.stripe.automation.ui.pages.PaymentsPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;
import org.testng.SkipException;

public class StripeDashboardSteps {
    private final LoginPage loginPage = new LoginPage(WebDriverFactory.getDriver());
    private final PaymentsPage paymentsPage = new PaymentsPage(WebDriverFactory.getDriver());

    @Given("the user is logged into Stripe dashboard")
    public void userLogin() {
        String url = ConfigManager.get("stripe.dashboard.url");
        String email = ConfigManager.get("stripe.dashboard.email");
        String password = ConfigManager.get("stripe.dashboard.password");

        if (url == null || url.isBlank() || email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new SkipException("Skipping UI scenario: Stripe dashboard credentials are not configured");
        }

        loginPage.open(url);
        loginPage.login(email, password);
    }

    @When("the user opens payments page")
    public void openPayments() {
        paymentsPage.openPayments();
    }

    @Then("payments page should be displayed")
    public void paymentsDisplayed() {
        Assert.assertTrue(paymentsPage.isLoaded());
    }

    @And("the user searches payment id {string}")
    public void searchPayment(String paymentId) {
        paymentsPage.searchPayment(paymentId);
    }

    @Then("refund status {string} should be visible")
    public void validateRefundStatus(String status) {
        Assert.assertTrue(paymentsPage.isRefundStatusVisible(status));
    }

    @Then("filters and pagination are available")
    public void validateFiltersPagination() {
        Assert.assertTrue(paymentsPage.filtersAndPaginationAvailable());
    }
}
