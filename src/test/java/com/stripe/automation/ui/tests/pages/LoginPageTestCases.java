package com.stripe.automation.ui.tests.pages;

import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginPageTestCases extends UiTestSupport {

    @Test(groups = {"ui", "smoke", "regression"})
    public void tcLogin001_userCanLoginAndLandOnDashboard() {
        loginOrSkip();
        Assert.assertTrue(dashboardHomePage.isLoaded(), "Dashboard should be visible after successful login");
    }
}
