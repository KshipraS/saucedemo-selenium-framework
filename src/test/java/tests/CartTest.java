package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.CartPage;
import pages.InventoryPage;
import pages.LoginPage;
import utils.ExtentManager;
import utils.ProductCatalog;
import utils.ProductCatalog.Product;

import java.util.List;

/**
 * Covers TC 3 (add to cart + badge count) and TC 5 (remove item from cart).
 * Product identity (slug + display name) comes from utils.ProductCatalog
 * (Products sheet in TestData.xlsx), not hardcoded string literals - so a
 * catalog rename/price change only needs a one-line edit in the sheet.
 */
public class CartTest extends BaseTest {

    private static final Product BACKPACK = ProductCatalog.get("sauce-labs-backpack");
    private static final Product BIKE_LIGHT = ProductCatalog.get("sauce-labs-bike-light");

    @Test(description = "Add two items to cart and verify badge count and cart contents")
    public void addToCartAndVerifyBadgeTest() {
        InventoryPage inventoryPage = new LoginPage(getDriver()).loginAs("standard_user", "secret_sauce");
        ExtentManager.logStep(getDriver(), "Logged in - Inventory page");
        inventoryPage.addToCartByProductSlug(BACKPACK.slug());
        inventoryPage.addToCartByProductSlug(BIKE_LIGHT.slug());
        ExtentManager.logStep(getDriver(), "Added " + BACKPACK.name() + " and " + BIKE_LIGHT.name() + " to cart");
        Assert.assertEquals(inventoryPage.getCartBadgeCount(), 2, "Cart badge should show 2 items");

        CartPage cartPage = inventoryPage.goToCart();
        ExtentManager.logStep(getDriver(), "Navigated to Cart page");
        List<String> itemNames = cartPage.getCartItemNames();
        Assert.assertEquals(itemNames.size(), 2, "Cart page should list 2 items");
        Assert.assertTrue(itemNames.contains(BACKPACK.name()), "Cart should contain " + BACKPACK.name());
        Assert.assertTrue(itemNames.contains(BIKE_LIGHT.name()), "Cart should contain " + BIKE_LIGHT.name());
    }

    @Test(description = "Add two items, remove one, and verify cart badge and contents update")
    public void removeItemFromCartTest() {
        InventoryPage inventoryPage = new LoginPage(getDriver()).loginAs("standard_user", "secret_sauce");
        ExtentManager.logStep(getDriver(), "Logged in - Inventory page");
        inventoryPage.addToCartByProductSlug(BACKPACK.slug());
        inventoryPage.addToCartByProductSlug(BIKE_LIGHT.slug());
        ExtentManager.logStep(getDriver(), "Added " + BACKPACK.name() + " and " + BIKE_LIGHT.name() + " to cart");
        Assert.assertEquals(inventoryPage.getCartBadgeCount(), 2, "Cart badge should show 2 items before removal");

        CartPage cartPage = inventoryPage.goToCart();
        Assert.assertEquals(cartPage.getItemCount(), 2, "Cart should list 2 items before removal");

        cartPage.removeItemBySlug(BIKE_LIGHT.slug());
        ExtentManager.logStep(getDriver(), "Removed " + BIKE_LIGHT.name() + " from cart");
        List<String> remainingItems = cartPage.getCartItemNames();
        Assert.assertEquals(remainingItems.size(), 1, "Cart should list 1 item after removal");
        Assert.assertTrue(remainingItems.contains(BACKPACK.name()), BACKPACK.name() + " should remain in cart");
        Assert.assertFalse(remainingItems.contains(BIKE_LIGHT.name()), BIKE_LIGHT.name() + " should be removed");
    }
}
