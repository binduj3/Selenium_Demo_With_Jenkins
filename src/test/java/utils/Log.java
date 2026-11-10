package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Thin log4j2 wrapper. log4j2.xml prints the thread name, so parallel output stays readable. */
public class Log {

    private static final Logger LOGGER = LogManager.getLogger(Log.class);

    public static void startTestCase(String name) {
        LOGGER.info("========== START: {} ==========", name);
    }

    public static void endTestCase(String name) {
        LOGGER.info("========== END:   {} ==========", name);
    }

    public static void info(String message)  { LOGGER.info(message); }
    public static void warn(String message)  { LOGGER.warn(message); }
    public static void error(String message) { LOGGER.error(message); }
    public static void debug(String message) { LOGGER.debug(message); }
}
