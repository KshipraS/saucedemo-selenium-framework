package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.Utility;

import java.util.List;
import java.util.stream.Collectors;

public class InventoryPage {

    private final WebDriver driver;
    private final Utility utility;

    private final By pageTitle = By.cssSelector("[data-test='title']");
    private final By sortDropdown = By.cssSelector("[data-test='product-sort-container']");
    private final By productNames = By.cssSelector(".inventory_item_name");
    private final By productPrices = By.cssSelector(".inventory_item_price");
    private final By cartLink = By.cssSelector(".shopping_cart_link");
    private final By cartBadge = By.cssSelector(".shopping_cart_badge");

    public InventoryPage(WebDriver driver) {
        this.driver = driver;
        this.utility = new Utility(driver);
    }

    public String getPageHeading() {
        return utility.getText(pageTitle);
    }

    public InventoryPage sortBy(String visibleOptionText) {
        utility.selectByVisibleText(sortDropdown, visibleOptionText);
        return this;
    }

    public List<String> getProductNames() {
        return utility.findAllVisible(productNames).stream()
                .map(e -> e.getText())
                .collect(Collectors.toList());
    }

    public List<Double> getProductPrices() {
        return utility.findAllVisible(productPrices).stream()
                .map(e -> Double.parseDouble(e.getText().replace("$", "").trim()))
                .collect(Collectors.toList());
    }

    public InventoryPage addToCartByProductSlug(String slug) {
        By addToCartButton = By.cssSelector("[data-test='add-to-cart-" + slug + "']");
        utility.click(addToCartButton);
        return this;
    }

    public int getCartBadgeCount() {
        List<org.openqa.selenium.WebElement> badge = utility.findAll(cartBadge);
        if (badge.isEmpty()) return 0;
        return Integer.parseInt(badge.get(0).getText().trim());
    }

    public CartPage goToCart() {
        utility.click(cartLink);
        return new CartPage(driver);
    }
}
