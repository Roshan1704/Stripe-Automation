package com.stripe.automation.ui.tests.pages;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DisputesPageTestCases extends UiTestSupport {

    @Test(groups = {"ui", "regression"})
    public void tcDisputes001_userCanOpenDisputesPage() {
        loginOrSkip();
        dashboardHomePage.openDisputes();
        Assert.assertTrue(disputesPage.isLoaded(), "Disputes page should be loaded");
    }
}
