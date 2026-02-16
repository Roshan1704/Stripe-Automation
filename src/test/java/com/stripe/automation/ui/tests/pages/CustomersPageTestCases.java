package com.stripe.automation.ui.tests.pages;

import org.testng.Assert;
import org.testng.annotations.Test;

public class CustomersPageTestCases extends UiTestSupport {

    @Test(groups = {"ui", "regression"})
    public void tcCustomers001_userCanOpenCustomersPage() {
        loginOrSkip();
        dashboardHomePage.openCustomers();
        Assert.assertTrue(customersPage.isLoaded(), "Customers page should be loaded");
    }

    @Test(groups = {"ui", "regression"})
    public void tcCustomers002_userCanSearchCustomer() {
        loginOrSkip();
        dashboardHomePage.openCustomers();
        Assert.assertTrue(customersPage.isLoaded(), "Customers page should be loaded");
        customersPage.searchCustomer("cus_test_001");
    }
}
