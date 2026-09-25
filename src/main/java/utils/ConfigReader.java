package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads config.properties from the classpath (src/test/resources).
 * Using the classpath instead of a hardcoded filesystem path means this
 * works identically on Windows, Linux, CI runners, and inside a packaged jar -
 * no System.getProperty("user.dir") path-building, which is the pattern that
 * breaks the moment the project is built or run from a different working directory.
 */
public class ConfigReader {

    private static final Logger log = LogManager.getLogger(ConfigReader.class);
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = ConfigReader.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("config.properties not found on classpath (expected under src/test/resources)");
            }
            properties.load(input);
            log.info("Loaded config.properties successfully");
        } catch (IOException e) {
            log.error("Failed to load config.properties", e);
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    public static String get(String key) {
        // System property override lets CI pass -Dbrowser=firefox etc. without editing the properties file.
        String override = System.getProperty(key);
        if (override != null && !override.isEmpty()) {
            return override;
        }
        String value = properties.getProperty(key);
        if (value == null) {
            log.warn("Property '{}' not found in config.properties", key);
        }
        return value;
    }

    public static String get(String key, String defaultValue) {
        String value = get(key);
        return (value == null || value.isEmpty()) ? defaultValue : value;
    }

    public static int getInt(String key, int defaultValue) {
        String value = get(key);
        return (value == null || value.isEmpty()) ? defaultValue : Integer.parseInt(value.trim());
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        return (value == null || value.isEmpty()) ? defaultValue : Boolean.parseBoolean(value.trim());
    }
}
