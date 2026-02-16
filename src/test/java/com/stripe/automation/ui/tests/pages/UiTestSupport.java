package com.stripe.automation.ui.tests.pages;

import com.stripe.automation.base.BaseUiTest;
import com.stripe.automation.config.ConfigManager;
import com.stripe.automation.support.TestPrerequisites;
import com.stripe.automation.ui.pages.CustomersPage;
import com.stripe.automation.ui.pages.DashboardHomePage;
import com.stripe.automation.ui.pages.DisputesPage;
import com.stripe.automation.ui.pages.LoginPage;
import com.stripe.automation.ui.pages.PaymentsPage;
import com.stripe.automation.ui.pages.RefundsPage;
import org.testng.annotations.BeforeMethod;

public abstract class UiTestSupport extends BaseUiTest {
    protected LoginPage loginPage;
    protected DashboardHomePage dashboardHomePage;
    protected PaymentsPage paymentsPage;
    protected CustomersPage customersPage;
    protected RefundsPage refundsPage;
    protected DisputesPage disputesPage;

    @BeforeMethod(alwaysRun = true)
    public void initPages() {
        loginPage = new LoginPage(driver);
        dashboardHomePage = new DashboardHomePage(driver);
        paymentsPage = new PaymentsPage(driver);
        customersPage = new CustomersPage(driver);
        refundsPage = new RefundsPage(driver);
        disputesPage = new DisputesPage(driver);
    }

    protected void loginOrSkip() {
        String email = ConfigManager.get("stripe.dashboard.email");
        String password = ConfigManager.get("stripe.dashboard.password");
        String url = ConfigManager.get("stripe.dashboard.url");

        TestPrerequisites.requireUiCredentials();
        loginPage.open(url);
        loginPage.login(email, password);
    }
}
