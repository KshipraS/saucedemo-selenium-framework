package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.Utility;

public class CheckoutCompletePage {

    private final Utility utility;
    private final By completeHeader = By.cssSelector(".complete-header");

    public CheckoutCompletePage(WebDriver driver) {
        this.utility = new Utility(driver);
    }

    public String getConfirmationMessage() {
        return utility.getText(completeHeader);
    }
}
