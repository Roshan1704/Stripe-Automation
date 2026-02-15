package com.stripe.automation.utils;

import java.util.UUID;

public final class CorrelationId {
    private static final ThreadLocal<String> ID = ThreadLocal.withInitial(() -> UUID.randomUUID().toString());

    private CorrelationId() {}

    public static String get() {
        return ID.get();
    }

    public static void refresh() {
        ID.set(UUID.randomUUID().toString());
    }
}
