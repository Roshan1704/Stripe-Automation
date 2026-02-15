package com.stripe.automation.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ConfigManager {
    private static final Properties PROPERTIES = new Properties();
    private static final Pattern ENV_PATTERN = Pattern.compile("\\$\\{(.+?)}");

    static {
        String env = System.getProperty("env", System.getenv().getOrDefault("ENV", "test"));
        String resource = "config/" + env + ".properties";
        try (InputStream input = ConfigManager.class.getClassLoader().getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalStateException("Config file not found: " + resource);
            }
            PROPERTIES.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load config file: " + resource, e);
        }
    }

    private ConfigManager() {}

    public static String get(String key) {
        String raw = System.getProperty(key, PROPERTIES.getProperty(key));
        if (raw == null) {
            return null;
        }
        Matcher matcher = ENV_PATTERN.matcher(raw);
        if (matcher.matches()) {
            return System.getenv(matcher.group(1));
        }
        return raw;
    }
}
