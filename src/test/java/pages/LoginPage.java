package pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import utils.CommonMethods;

public class LoginPage extends CommonMethods {

    @FindBy(name = "user-name")
    public WebElement usernameInput;

    @FindBy(name = "password")
    public WebElement passwordInput;

    @FindBy(name = "login-button")
    public WebElement loginButton;

    @FindBy(css = "[data-test='error']")
    public WebElement errorMessage;

    public LoginPage() {
        PageFactory.initElements(getDriver(), this);
    }

    public void enterUsername(String username) {
        clearAndSendText(username, usernameInput);
    }

    public void enterPassword(String password) {
        clearAndSendText(password, passwordInput);
    }

    public void clickLoginButton() {
        click(loginButton);
    }

    public boolean isUsernameFieldDisplayed() {
        waitForElementToBeVisible(usernameInput);
        return usernameInput.isDisplayed();
    }

    public boolean isPasswordFieldDisplayed() {
        waitForElementToBeVisible(passwordInput);
        return passwordInput.isDisplayed();
    }

    public boolean isLoginButtonDisplayed() {
        waitForElementToBeVisible(loginButton);
        return loginButton.isDisplayed();
    }

    public boolean isLoginButtonEnabled() {
        waitForElementToBeVisible(loginButton);
        return loginButton.isEnabled();
    }

    public boolean isErrorMessageDisplayed() {
        waitForElementToBeVisible(errorMessage);
        return errorMessage.isDisplayed();
    }

    // errorMessage.getText() correctly excludes the close button's text since the
    // button contains only an SVG icon and no visible text node.
    public String getErrorMessageText() {
        waitForElementToBeVisible(errorMessage);
        return errorMessage.getText().trim();
    }

    public boolean isRedirectedToUrl(String expectedUrl) {
        waitForUrl(expectedUrl);
        return getDriver().getCurrentUrl().equals(expectedUrl);
    }
}
