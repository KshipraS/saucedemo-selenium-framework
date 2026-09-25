package utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.JavascriptExecutor;

import java.util.List;

/**
 * Common Selenium element actions used across page classes.
 * Keeps synchronization and low-level WebElement interactions in one place.
 */
public class Utility {

    private final WebDriver driver;
    private final WaitUtils wait;

    public Utility(WebDriver driver) {
        this.driver = driver;
        this.wait = new WaitUtils(driver);
    }

    public void click(By locator) {
        wait.waitForClickable(locator).click();
    }

    public void enterText(By locator, String text) {
        WebElement element = wait.waitForVisibility(locator);
        element.clear();
        element.sendKeys(text);
    }

    public String getText(By locator) {
        return wait.waitForVisibility(locator).getText();
    }

    public WebElement find(By locator) {
        return wait.waitForVisibility(locator);
    }

    public List<WebElement> findAll(By locator) {
        return wait.findAll(locator);
    }

    public List<WebElement> findAllVisible(By locator) {
        return wait.waitForVisibilityOfAll(locator);
    }

    public void selectByVisibleText(By locator, String visibleText) {
        new Select(wait.waitForVisibility(locator)).selectByVisibleText(visibleText);
    }

    public void scrollToElement(By locator) {
        WebElement element = wait.waitForVisibility(locator);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});", element);
    }
}
