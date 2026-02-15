package com.stripe.automation.listeners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {
    private int count;
    private static final int MAX_RETRIES = 2;

    @Override
    public boolean retry(ITestResult result) {
        return count++ < MAX_RETRIES;
    }
}
