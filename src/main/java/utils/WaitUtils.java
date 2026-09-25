package utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class WaitUtils 
{

    private final WebDriver driver;
    private final WebDriverWait wait;

    public WaitUtils(WebDriver driver) 
    {
        this.driver = driver;
        int timeoutSeconds = ConfigReader.getInt("explicit.wait.seconds", 15);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    }

    public WebElement waitForVisibility(By locator) 
    {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement waitForClickable(By locator) 
    {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public List<WebElement> waitForVisibilityOfAll(By locator) 
    {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    /**
     * No wait - used where finding zero elements is a valid state (e.g. the cart badge locator matches nothing when the cart is empty; waiting for
     * visibility there would time out instead of correctly returning "0 items").
     */
    public List<WebElement> findAll(By locator) 
    {
        return driver.findElements(locator);
    }
}
