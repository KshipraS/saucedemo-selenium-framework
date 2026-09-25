package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.Utility;

public class CheckoutStepOnePage {

    private final WebDriver driver;
    private final Utility utility;

    private final By firstNameInput = By.cssSelector("[data-test='firstName']");
    private final By lastNameInput = By.cssSelector("[data-test='lastName']");
    private final By postalCodeInput = By.cssSelector("[data-test='postalCode']");
    private final By continueButton = By.cssSelector("[data-test='continue']");

    public CheckoutStepOnePage(WebDriver driver) {
        this.driver = driver;
        this.utility = new Utility(driver);
    }

    public CheckoutStepTwoPage fillInfoAndContinue(String firstName, String lastName, String zip) {
        utility.enterText(firstNameInput, firstName);
        utility.enterText(lastNameInput, lastName);
        utility.enterText(postalCodeInput, zip);
        utility.click(continueButton);
        return new CheckoutStepTwoPage(driver);
    }
}
