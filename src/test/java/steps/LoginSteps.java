package steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import utils.CommonMethods;
import utils.ConfigReader;
import utils.Constants;
import utils.ExcelReader;
import utils.Log;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class LoginSteps extends CommonMethods {

    // The @Before hook already opened the browser on the login URL; this just confirms we're there.
    @Given("user navigates to the SauceDemo login page")
    public void userNavigatesToTheSauceDemoLoginPage() {
        String url = ConfigReader.read("url");
        if (!getDriver().getCurrentUrl().startsWith(url)) {
            getDriver().get(url);
        }
        assertTrue(loginPage().isUsernameFieldDisplayed(), "Login page did not load");
        Log.info("On login page: " + url);
    }

    @Then("user is able to see the {string} input field")
    public void userIsAbleToSeeTheInputField(String field) {
        boolean displayed = switch (field.toLowerCase()) {
            case "username" -> loginPage().isUsernameFieldDisplayed();
            case "password" -> loginPage().isPasswordFieldDisplayed();
            default -> throw new IllegalArgumentException("Unknown input field: " + field);
        };
        assertTrue(displayed, field + " input field is not displayed");
    }

    @Then("user is able to see the {string} button")
    public void userIsAbleToSeeTheButton(String button) {
        assertLoginButton(button);
        assertTrue(loginPage().isLoginButtonDisplayed(), "Login button is not displayed");
    }

    @Then("user is able to see that the {string} button is enabled")
    public void userIsAbleToSeeThatTheButtonIsEnabled(String button) {
        assertLoginButton(button);
        assertTrue(loginPage().isLoginButtonEnabled(), "Login button is not enabled");
    }

    @When("user enters {string} into the Username field")
    public void userEntersIntoTheUsernameField(String username) {
        loginPage().enterUsername(username);
    }

    @When("user enters {string} into the Password field")
    public void userEntersIntoThePasswordField(String password) {
        loginPage().enterPassword(password);
    }

    @When("user leaves the Username field empty")
    public void userLeavesTheUsernameFieldEmpty() {
        loginPage().enterUsername("");
    }

    @When("user leaves the Password field empty")
    public void userLeavesThePasswordFieldEmpty() {
        loginPage().enterPassword("");
    }

    @When("user clicks the Login button")
    public void userClicksTheLoginButton() {
        loginPage().clickLoginButton();
    }

    @Then("user is able to see the Products page")
    public void userIsAbleToSeeTheProductsPage() {
        waitForUrl(Constants.INVENTORY_PATH);
        assertTrue(getDriver().getCurrentUrl().contains(Constants.INVENTORY_PATH),
                "Not redirected to Products page, current URL: " + getDriver().getCurrentUrl());
    }

    @Then("user is not able to log in")
    public void userIsNotAbleToLogIn() {
        assertTrue(loginPage().isErrorMessageDisplayed(), "Expected an error message after a failed login");
        assertFalse(getDriver().getCurrentUrl().contains(Constants.INVENTORY_PATH),
                "User was logged in but should not have been");
    }

    @Then("user is able to see the error message {string}")
    public void userIsAbleToSeeTheErrorMessage(String expected) {
        assertEquals(expected, loginPage().getErrorMessageText());
    }

    @When("user logs in using credentials from the {string} sheet in the Excel test data file")
    public void userLogsInUsingCredentialsFromExcel(String sheetName) {
        List<Map<String, String>> rows = ExcelReader.read(Constants.TESTDATA_FILEPATH, sheetName);
        assertFalse(rows.isEmpty(), "No data rows in sheet " + sheetName);

        Map<String, String> row = rows.get(0);
        loginPage().enterUsername(row.get("Username"));
        loginPage().enterPassword(row.get("Password"));
        loginPage().clickLoginButton();
        Log.info("Logged in with Excel user: " + row.get("Username"));
    }

    private void assertLoginButton(String button) {
        assertEquals("login", button.toLowerCase(), "Only the Login button is supported on this page");
    }
}
