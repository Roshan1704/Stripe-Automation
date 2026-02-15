package com.stripe.automation.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public final class TestDataLoader {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private TestDataLoader() {}

    public static Map<String, Object> loadJson(String resource) {
        try (InputStream input = TestDataLoader.class.getClassLoader().getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalArgumentException("Missing resource: " + resource);
            }
            return MAPPER.readValue(input, new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Map<String, String>> loadJsonArray(String resource) {
        try (InputStream input = TestDataLoader.class.getClassLoader().getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalArgumentException("Missing resource: " + resource);
            }
            return MAPPER.readValue(input, new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
