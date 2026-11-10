package utils;

public class Constants {

    private static final String PROJECT_DIR = System.getProperty("user.dir");

    public static final String CONFIG_FILE_PATH =
            PROJECT_DIR + "/src/test/resources/config/config.properties";

    public static final String TESTDATA_FILEPATH =
            PROJECT_DIR + "/src/test/resources/testData/TestData.xlsx";

    public static final String SCREENSHOT_FILEPATH =
            PROJECT_DIR + "/target/screenshots/";

    public static final int EXPLICIT_WAIT = 20;

    public static final String INVENTORY_PATH = "inventory.html";
}
