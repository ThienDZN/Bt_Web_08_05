package vn.iotstar.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AppProperties {
    private static final Properties PROPERTIES = load();
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^:}]+)(?::([^}]*))?}");

    private AppProperties() {
    }

    public static String get(String key, String defaultValue) {
        String value = resolve(PROPERTIES.getProperty(key));
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }

    public static int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(get(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(get(key, String.valueOf(defaultValue)));
    }

    private static Properties load() {
        Properties properties = new Properties();
        try (InputStream inputStream = AppProperties.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException ignored) {
        }
        return properties;
    }

    private static String resolve(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return rawValue;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(rawValue);
        StringBuffer resolved = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            String fallback = matcher.group(2);
            String replacement = System.getProperty(key);
            if (replacement == null || replacement.isBlank()) {
                replacement = System.getenv(key);
            }
            if (replacement == null) {
                replacement = fallback == null ? "" : fallback;
            }
            matcher.appendReplacement(resolved, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(resolved);
        return resolved.toString();
    }
}
