package com.stripe.automation.ui.tests.pages;

import org.testng.Assert;
import org.testng.annotations.Test;

public class PaymentsPageTestCases extends UiTestSupport {

    @Test(groups = {"ui", "regression"})
    public void tcPayments001_userCanOpenPaymentsPage() {
        loginOrSkip();
        dashboardHomePage.openPayments();
        Assert.assertTrue(paymentsPage.isLoaded(), "Payments page should be loaded");
    }

    @Test(groups = {"ui", "regression"})
    public void tcPayments002_userCanSearchPaymentId() {
        loginOrSkip();
        dashboardHomePage.openPayments();
        Assert.assertTrue(paymentsPage.isLoaded(), "Payments page should be loaded");
        paymentsPage.searchPayment("pi_test_123");
    }
}
