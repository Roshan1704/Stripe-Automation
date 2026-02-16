package com.stripe.automation.unit;

import com.stripe.automation.utils.TestDataLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

public class TestDataLoaderTests {

    @Test
    public void shouldLoadJsonObjectFromResource() {
        Map<String, Object> data = TestDataLoader.loadJson("testdata/payment-data.json");
        Assert.assertTrue(data.containsKey("validCard"));
    }

    @Test
    public void shouldLoadJsonArrayFromResource() {
        List<Map<String, String>> dataset = TestDataLoader.loadJsonArray("testdata/payment-data-array.json");
        Assert.assertEquals(dataset.size(), 2);
        Assert.assertEquals(dataset.get(0).get("currency"), "usd");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowWhenJsonResourceMissing() {
        TestDataLoader.loadJson("testdata/does-not-exist.json");
    }
}
