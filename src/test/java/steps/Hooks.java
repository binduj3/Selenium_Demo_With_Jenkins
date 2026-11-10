package steps;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import utils.CommonMethods;
import utils.ConfigReader;

/**
 * Every scenario gets its own browser on its own thread, opened here and always closed here.
 * Don't call openBrowser() from step definitions (e.g. the Background "navigates to" step),
 * or you'll open a second browser.
 */
public class Hooks extends CommonMethods {

    @Before
    public void setUp(Scenario scenario) {
        openBrowser();
        scenario.log("Browser: " + ConfigReader.readOrDefault("browser", "chrome")
                + " | thread: " + Thread.currentThread().getName());
    }

    @After
    public void tearDown(Scenario scenario) {
        try {
            if (scenario.isFailed() && hasDriver()) {
                byte[] screenshot = takeScreenshot(scenario.getName());
                scenario.attach(screenshot, "image/png", scenario.getName());
            }
        } finally {
            closeBrowser();
        }
    }
}
