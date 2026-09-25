package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.WaitUtils;

public class CheckoutStepTwoPage {

    private final WebDriver driver;
    private final WaitUtils wait;

    private final By itemTotalLabel = By.cssSelector(".summary_subtotal_label");
    private final By taxLabel = By.cssSelector(".summary_tax_label");
    private final By totalLabel = By.cssSelector(".summary_total_label");
    private final By finishButton = By.cssSelector("[data-test='finish']");

    public CheckoutStepTwoPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WaitUtils(driver);
    }

    /** e.g. "Item total: $39.98" -> 39.98 */
    public double getItemTotal() {
        return parseAmount(wait.waitForVisibility(itemTotalLabel).getText());
    }

    public double getTax() {
        return parseAmount(driver.findElement(taxLabel).getText());
    }

    public double getTotal() {
        return parseAmount(driver.findElement(totalLabel).getText());
    }

    private double parseAmount(String labelText) {
        String amount = labelText.substring(labelText.indexOf('$') + 1).trim();
        return Double.parseDouble(amount);
    }

    public CheckoutCompletePage clickFinish() {
        wait.waitForClickable(finishButton).click();
        return new CheckoutCompletePage(driver);
    }
}
