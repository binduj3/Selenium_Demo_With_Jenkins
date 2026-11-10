package utils;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CommonMethods extends PageInitialiser {

    /** One driver per thread -> safe for parallel scenarios. */
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private static final Duration GRID_STARTUP_TIMEOUT = Duration.ofSeconds(90);

    public static WebDriver getDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException("No WebDriver on thread " + Thread.currentThread().getName()
                    + " - did the @Before hook call openBrowser()?");
        }
        return driver;
    }

    public static boolean hasDriver() {
        return DRIVER.get() != null;
    }

    // ------------------------------------------------------------------ browser lifecycle

    /**
     * Opens chrome / edge / firefox / safari.
     *  - browser : -Dbrowser=..., BROWSER env var, or config.properties (case-insensitive)
     *  - headless: -Dheadless=true ...
     *  - gridUrl : if set (e.g. http://localhost:4444) a RemoteWebDriver is created on Selenium Grid,
     *              otherwise a local driver is started via Selenium Manager.
     */
    public void openBrowser() {
        String browser = ConfigReader.readOrDefault("browser", "chrome").toLowerCase();
        boolean headless = Boolean.parseBoolean(ConfigReader.readOrDefault("headless", "false"));
        String gridUrl = ConfigReader.readOrDefault("gridUrl", "");

        MutableCapabilities options = buildOptions(browser, headless);
        WebDriver driver = gridUrl.isBlank()
                ? createLocalDriver(browser, options)
                : createRemoteDriver(gridUrl, options);
        DRIVER.set(driver);

        if (headless || !gridUrl.isBlank()) {
            driver.manage().window().setSize(new Dimension(1920, 1080));
        } else {
            driver.manage().window().maximize();
        }

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
        driver.get(ConfigReader.read("url"));

        initializePageObjects();
    }

    public void closeBrowser() {
        WebDriver driver = DRIVER.get();
        try {
            if (driver != null) {
                driver.quit();
            }
        } finally {
            DRIVER.remove();
            clearPageObjects();
        }
    }

    private MutableCapabilities buildOptions(String browser, boolean headless) {
        // Stops Chrome/Edge "change your password" / save-password popups that break SauceDemo logins
        Map<String, Object> noPasswordPopups = Map.of(
                "credentials_enable_service", false,
                "profile.password_manager_enabled", false,
                "profile.password_manager_leak_detection", false);

        switch (browser) {
            case "chrome": {
                ChromeOptions o = new ChromeOptions();
                o.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu");
                o.setExperimentalOption("prefs", noPasswordPopups);
                if (headless) o.addArguments("--headless=new", "--window-size=1920,1080");
                return o;
            }
            case "edge": {
                EdgeOptions o = new EdgeOptions();
                o.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu");
                o.setExperimentalOption("prefs", noPasswordPopups);
                if (headless) o.addArguments("--headless=new", "--window-size=1920,1080");
                return o;
            }
            case "firefox": {
                FirefoxOptions o = new FirefoxOptions();
                if (headless) o.addArguments("-headless", "--width=1920", "--height=1080");
                return o;
            }
            case "safari":
                // Safari has no headless mode and allows only ONE session per machine -> run it with 1 thread
                return new SafariOptions();
            default:
                throw new IllegalArgumentException("Unsupported browser '" + browser
                        + "'. Use chrome, edge, firefox or safari.");
        }
    }

    private WebDriver createLocalDriver(String browser, MutableCapabilities options) {
        switch (browser) {
            case "chrome":  return new ChromeDriver((ChromeOptions) options);
            case "edge":    return new EdgeDriver((EdgeOptions) options);
            case "firefox": return new FirefoxDriver((FirefoxOptions) options);
            case "safari":  return new SafariDriver((SafariOptions) options);
            default: throw new IllegalArgumentException("Unsupported browser: " + browser);
        }
    }

    /** Retries while the Grid container is still starting up. */
    private WebDriver createRemoteDriver(String gridUrl, MutableCapabilities options) {
        long deadline = System.nanoTime() + GRID_STARTUP_TIMEOUT.toNanos();
        while (true) {
            try {
                RemoteWebDriver driver = new RemoteWebDriver(URI.create(gridUrl).toURL(), options);
                driver.setFileDetector(new LocalFileDetector());
                return driver;
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Bad gridUrl: " + gridUrl, e);
            } catch (WebDriverException e) {
                if (System.nanoTime() > deadline) {
                    throw new WebDriverException("Selenium Grid at " + gridUrl + " not ready after "
                            + GRID_STARTUP_TIMEOUT.toSeconds() + "s", e);
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }
    }

    // ------------------------------------------------------------------ element helpers

    public void sendText(String text, WebElement element) {
        element.clear();
        element.sendKeys(text);
    }

    public void clearAndSendText(String text, WebElement element) {
        element.click();
        // COMMAND on macOS, CONTROL elsewhere
        Keys selectAllKey = Platform.getCurrent().is(Platform.MAC) ? Keys.COMMAND : Keys.CONTROL;
        element.sendKeys(Keys.chord(selectAllKey, "a"));
        element.sendKeys(Keys.DELETE);
        if (text != null && !text.isBlank()) {
            element.sendKeys(text);
        }
    }

    public WebDriverWait getwait() {
        return new WebDriverWait(getDriver(), Duration.ofSeconds(Constants.EXPLICIT_WAIT));
    }

    public void waitForElementToBeClickAble(WebElement element) {
        getwait().until(ExpectedConditions.elementToBeClickable(element));
    }

    public void click(WebElement element) {
        waitForElementToBeClickAble(element);
        element.click();
    }

    public void click(WebElement element, boolean clickableAndOptionReady) {
        if (clickableAndOptionReady) {
            waitForOptionReadyAndClickable(element);
        } else {
            waitForElementToBeClickAble(element);
        }
        element.click();
    }

    public void waitForOptionReadyAndClickable(WebElement element) {
        getwait().until(
                ExpectedConditions.and(
                        ExpectedConditions.elementToBeClickable(element),
                        ExpectedConditions.not(ExpectedConditions.textToBePresentInElement(element, "Searching..."))
                )
        );
    }

    public void waitForTextToBe(WebElement element, String expectedText) {
        getwait().until(d -> element.getText().trim().equalsIgnoreCase(expectedText));
    }

    public void waitForUrl(String url) {
        getwait().until(ExpectedConditions.urlContains(url));
    }

    public void waitForElementToBeVisible(WebElement element) {
        getwait().until(ExpectedConditions.refreshed(ExpectedConditions.visibilityOf(element)));
    }

    public void waitForValueToBePopulated(WebElement element) {
        getwait().until(ExpectedConditions.attributeToBeNotEmpty(element, "value"));
    }

    // Generates random number between 1 and 1000
    public int generateRandomNumber() {
        return (int) (Math.random() * 1000) + 1;
    }

    public void selectOptionByText(List<WebElement> options, String optionToSelect) {
        for (var element : options) {
            if (element.getText().equals(optionToSelect)) {
                click(element);
                break;
            }
        }
    }

    // ------------------------------------------------------------------ screenshots & misc

    public byte[] takeScreenshot(String fileName) {
        byte[] picBytes = ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.BYTES);
        String browser = ConfigReader.readOrDefault("browser", "chrome").toLowerCase();
        // browser + thread id in the name so parallel screenshots never overwrite each other
        String safeName = fileName.replaceAll("[^A-Za-z0-9._-]", "_");
        File target = new File(Constants.SCREENSHOT_FILEPATH + safeName + "_" + browser
                + "_t" + Thread.currentThread().threadId() + "_"
                + getTimeStamp("yyyy-MM-dd-HH-mm-ss-SSS") + ".png");
        try {
            FileUtils.writeByteArrayToFile(target, picBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return picBytes;
    }

    public String getTimeStamp(String pattern) {
        return new SimpleDateFormat(pattern).format(new Date());
    }

    public static String getUniqueId() {
        return String.valueOf((Math.random() * 10000) + 1);
    }
}
