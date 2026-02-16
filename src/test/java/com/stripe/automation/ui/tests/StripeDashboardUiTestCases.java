package com.stripe.automation.ui.tests;

import com.stripe.automation.base.BaseUiTest;
import com.stripe.automation.config.ConfigManager;
import com.stripe.automation.ui.pages.CustomersPage;
import com.stripe.automation.ui.pages.DashboardHomePage;
import com.stripe.automation.ui.pages.DisputesPage;
import com.stripe.automation.ui.pages.LoginPage;
import com.stripe.automation.ui.pages.PaymentsPage;
import com.stripe.automation.ui.pages.RefundsPage;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StripeDashboardUiTestCases extends BaseUiTest {
    private LoginPage loginPage;
    private DashboardHomePage dashboardHomePage;
    private PaymentsPage paymentsPage;
    private CustomersPage customersPage;
    private RefundsPage refundsPage;
    private DisputesPage disputesPage;

    @BeforeMethod(alwaysRun = true)
    public void pageSetup() {
        loginPage = new LoginPage(driver);
        dashboardHomePage = new DashboardHomePage(driver);
        paymentsPage = new PaymentsPage(driver);
        customersPage = new CustomersPage(driver);
        refundsPage = new RefundsPage(driver);
        disputesPage = new DisputesPage(driver);
    }

    private void loginOrSkip() {
        String email = ConfigManager.get("stripe.dashboard.email");
        String password = ConfigManager.get("stripe.dashboard.password");
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new SkipException("Dashboard credentials are not configured for UI tests");
        }

        loginPage.open(ConfigManager.get("stripe.dashboard.url"));
        loginPage.login(email, password);
    }

    @Test(groups = {"ui", "smoke"})
    public void tcUi001_userCanLoginAndLandOnDashboard() {
        loginOrSkip();
        Assert.assertTrue(dashboardHomePage.isLoaded());
    }

    @Test(groups = {"ui", "regression"})
    public void tcUi002_userCanSearchPaymentOnPaymentsPage() {
        loginOrSkip();
        dashboardHomePage.openPayments();
        Assert.assertTrue(paymentsPage.isLoaded());
        paymentsPage.searchPayment("pi_test_123");
    }

    @Test(groups = {"ui", "regression"})
    public void tcUi003_userCanOpenCustomersPage() {
        loginOrSkip();
        dashboardHomePage.openCustomers();
        Assert.assertTrue(customersPage.isLoaded());
        customersPage.searchCustomer("cus_test_001");
    }

    @Test(groups = {"ui", "regression"})
    public void tcUi004_userCanOpenRefundsPageAndSeeFilters() {
        loginOrSkip();
        dashboardHomePage.openRefunds();
        Assert.assertTrue(refundsPage.isLoaded());
        Assert.assertTrue(refundsPage.hasFilterControls());
    }

    @Test(groups = {"ui", "regression"})
    public void tcUi005_userCanOpenDisputesPage() {
        loginOrSkip();
        dashboardHomePage.openDisputes();
        Assert.assertTrue(disputesPage.isLoaded());
    }
}
