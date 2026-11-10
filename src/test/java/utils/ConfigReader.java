package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Properties;

/**
 * Lookup order for every key:
 *   1. JVM system property   (-Dbrowser=firefox)          -> used by Jenkins
 *   2. Environment variable  (BROWSER=firefox)
 *   3. config.properties
 * The default file is loaded once and shared read-only, which is thread-safe.
 */
public class ConfigReader {

    private static final Properties DEFAULT = load(Constants.CONFIG_FILE_PATH);

    public static String read(String key) {
        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank()) return sys.trim();

        String env = System.getenv(key.toUpperCase());
        if (env != null && !env.isBlank()) return env.trim();

        String value = DEFAULT.getProperty(key);
        return value == null ? null : value.trim();
    }

    public static String readOrDefault(String key, String defaultValue) {
        String value = read(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    /** Reads a key from another properties file (kept for backwards compatibility). */
    public static String read(String key, String path) {
        return load(path).getProperty(key);
    }

    private static Properties load(String path) {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(path)) {
            properties.load(fis);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read config file: " + path, e);
        }
        return properties;
    }
}
