package com.pcloudy.appium.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.Properties;

/**
 * Reads settings from config.properties. A value can be overridden by a JVM system property
 * (-Dkey=value) or by an environment variable in UPPER_SNAKE_CASE (pcloudy.api.key -> PCLOUDY_API_KEY).
 */
public final class Config {

    private static final Properties PROPERTIES = load();

    private Config() {
    }

    public static String get(String key) {
        String value = System.getProperty(key);
        if (isBlank(value)) {
            value = System.getenv(key.toUpperCase(Locale.ROOT).replace('.', '_'));
        }
        if (isBlank(value) && key.equals("pcloudy.api.key")) {
            value = System.getenv("PCLOUDY_APIKEY");
        }
        if (isBlank(value)) {
            value = PROPERTIES.getProperty(key, "");
        }
        return value.trim();
    }

    public static String require(String key) {
        String value = get(key);
        if (value.isEmpty()) {
            throw new IllegalStateException("Missing configuration value: " + key);
        }
        return value;
    }

    public static int getInt(String key) {
        return Integer.parseInt(require(key));
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static Properties load() {
        Properties properties = new Properties();
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in == null) {
                throw new IllegalStateException("config.properties not found on the test classpath");
            }
            properties.load(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return properties;
    }
}
