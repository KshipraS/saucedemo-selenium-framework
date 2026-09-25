package tests;

import base.BaseTest;
import dataproviders.ExcelDataProvider;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.*;
import utils.ConfigReader;
import utils.ExtentManager;
import utils.ProductCatalog;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Covers TC 4: end-to-end checkout. Data-driven from the "Checkout" sheet of
 * TestData.xlsx (shipping info + which product slugs to add), with expected
 * item total / tax / grand total computed from utils.ProductCatalog rather
 * than hardcoded - so this test stays correct if the sheet's CartItemSlugs
 * column is changed to a different product combination.
 *
 * Takes the whole row as a single Map<String,String> (keyed by the sheet's
 * header row) instead of one String parameter per column. With only 5
 * columns today that's a stylistic choice, but it's the version that scales
 * if the Checkout sheet grows past a handful of columns (e.g. billing
 * address, payment method, promo code, etc.) - adding a column becomes an
 * edit to the sheet plus one row.get("NewColumn") call, not a method
 * signature change everywhere the test is called.
 */
public class CheckoutTest extends BaseTest {

    @Test(dataProvider = "checkoutData", dataProviderClass = ExcelDataProvider.class,
          description = "Complete checkout end-to-end and verify computed totals and confirmation message")
    public void endToEndCheckoutTest(Map<String, String> row) {

        String testCaseId = row.get("TestCaseId");
        String firstName = row.get("FirstName");
        String lastName = row.get("LastName");
        String zipCode = row.get("ZipCode");
        String[] slugs = row.get("CartItemSlugs").split(",");

        InventoryPage inventoryPage = new LoginPage(getDriver()).loginAs("standard_user", "secret_sauce");
        ExtentManager.logStep(getDriver(), "Logged in - Inventory page");
        BigDecimal expectedItemTotal = BigDecimal.ZERO;
        for (String slug : slugs) {
            inventoryPage.addToCartByProductSlug(slug.trim());
            expectedItemTotal = expectedItemTotal.add(BigDecimal.valueOf(ProductCatalog.get(slug.trim()).price()));
        }
        ExtentManager.logStep(getDriver(), "Added items to cart: " + row.get("CartItemSlugs"));
        double taxRate = Double.parseDouble(ConfigReader.get("checkout.tax.rate", "0.08"));
        BigDecimal expectedTax = expectedItemTotal.multiply(BigDecimal.valueOf(taxRate))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedTotal = expectedItemTotal.add(expectedTax);

        CartPage cartPage = inventoryPage.goToCart();
        CheckoutStepOnePage stepOne = cartPage.clickCheckout();
        ExtentManager.logStep(getDriver(), "Navigated to Checkout - Step One");
        CheckoutStepTwoPage stepTwo = stepOne.fillInfoAndContinue(firstName, lastName, zipCode);
        ExtentManager.logStep(getDriver(), "Checkout Step Two - Order Summary");
        
        Assert.assertEquals(stepTwo.getItemTotal(), expectedItemTotal.doubleValue(), 0.001,
                "[" + testCaseId + "] Item total mismatch");
        Assert.assertEquals(stepTwo.getTax(), expectedTax.doubleValue(), 0.001,
                "[" + testCaseId + "] Tax mismatch");
        Assert.assertEquals(stepTwo.getTotal(), expectedTotal.doubleValue(), 0.001,
                "[" + testCaseId + "] Grand total mismatch");

        CheckoutCompletePage completePage = stepTwo.clickFinish();
        ExtentManager.logStep(getDriver(), "Order confirmation page reached");
        Assert.assertEquals(completePage.getConfirmationMessage(), "Thank you for your order!",
                "[" + testCaseId + "] Order confirmation message mismatch");
    }
}
