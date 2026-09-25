package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.Utility;

public class LoginPage {

    private final WebDriver driver;
    private final Utility utility;

    private final By usernameInput = By.cssSelector("[data-test='username']");
    private final By passwordInput = By.cssSelector("[data-test='password']");
    private final By loginButton = By.cssSelector("[data-test='login-button']");
    private final By errorMessage = By.cssSelector("[data-test='error']");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.utility = new Utility(driver);
    }

    public LoginPage enterUsername(String username) {
        utility.enterText(usernameInput, username);
        return this;
    }

    public LoginPage enterPassword(String password) {
        utility.enterText(passwordInput, password);
        return this;
    }

    public InventoryPage clickLogin() {
        utility.click(loginButton);
        return new InventoryPage(driver);
    }

    public void clickLoginExpectingFailure() {
        utility.click(loginButton);
    }

    public String getErrorMessage() {
        return utility.getText(errorMessage);
    }

    public InventoryPage loginAs(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        return clickLogin();
    }
}
