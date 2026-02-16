package com.stripe.automation.unit;

import com.stripe.automation.utils.CorrelationId;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CorrelationIdTests {

    @Test
    public void shouldReturnStableIdWithinSameThreadUntilRefresh() {
        String first = CorrelationId.get();
        String second = CorrelationId.get();
        Assert.assertEquals(first, second);
    }

    @Test
    public void shouldRegenerateIdAfterRefresh() {
        String initial = CorrelationId.get();
        CorrelationId.refresh();
        String refreshed = CorrelationId.get();
        Assert.assertNotEquals(initial, refreshed);
    }
}
