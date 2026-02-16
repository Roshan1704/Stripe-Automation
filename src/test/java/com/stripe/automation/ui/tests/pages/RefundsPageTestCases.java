package com.stripe.automation.ui.tests.pages;

import org.testng.Assert;
import org.testng.annotations.Test;

public class RefundsPageTestCases extends UiTestSupport {

    @Test(groups = {"ui", "regression"})
    public void tcRefunds001_userCanOpenRefundsPage() {
        loginOrSkip();
        dashboardHomePage.openRefunds();
        Assert.assertTrue(refundsPage.isLoaded(), "Refunds page should be loaded");
    }

    @Test(groups = {"ui", "regression"})
    public void tcRefunds002_filtersAreVisibleOnRefundsPage() {
        loginOrSkip();
        dashboardHomePage.openRefunds();
        Assert.assertTrue(refundsPage.isLoaded(), "Refunds page should be loaded");
        Assert.assertTrue(refundsPage.hasFilterControls(), "Refund filters should be available");
    }
}
