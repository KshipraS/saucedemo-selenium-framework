package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.Utility;

import java.util.List;
import java.util.stream.Collectors;

public class CartPage {

    private final WebDriver driver;
    private final Utility utility;

    private final By cartItems = By.cssSelector(".cart_item");
    private final By cartItemNames = By.cssSelector(".cart_item .inventory_item_name");
    private final By checkoutButton = By.cssSelector("[data-test='checkout']");

    public CartPage(WebDriver driver) {
        this.driver = driver;
        this.utility = new Utility(driver);
    }

    public List<String> getCartItemNames() {
        if (utility.findAll(cartItems).isEmpty()) return List.of();
        return utility.findAllVisible(cartItemNames).stream()
                .map(e -> e.getText())
                .collect(Collectors.toList());
    }

    public int getItemCount() {
        return utility.findAll(cartItems).size();
    }

    public CartPage removeItemBySlug(String slug) {
        By removeButton = By.cssSelector("[data-test='remove-" + slug + "']");
        utility.click(removeButton);
        return this;
    }

    public CheckoutStepOnePage clickCheckout() {
        utility.click(checkoutButton);
        return new CheckoutStepOnePage(driver);
    }
}
