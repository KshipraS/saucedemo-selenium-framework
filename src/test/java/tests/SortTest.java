package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.InventoryPage;
import pages.LoginPage;
import utils.ExtentManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Covers TC 2: product sort validation across all four sort options. */
public class SortTest extends BaseTest {

    @Test(description = "Verify product list re-orders correctly for each sort option")
    public void sortProductsTest() {
        InventoryPage inventoryPage = new LoginPage(getDriver()).loginAs("standard_user", "secret_sauce");
        ExtentManager.logStep(getDriver(), "Logged in - Inventory page (default sort: Name A to Z)");
        
        // Name (A to Z) - default on load
        List<String> namesAsc = inventoryPage.getProductNames();
        List<String> expectedAsc = new ArrayList<>(namesAsc);
        Collections.sort(expectedAsc);
        Assert.assertEquals(namesAsc, expectedAsc, "Default sort should be Name (A to Z)");

        // Name (Z to A)
        inventoryPage.sortBy("Name (Z to A)");
        ExtentManager.logStep(getDriver(), "Sorted by Name (Z to A)");
        List<String> namesDesc = inventoryPage.getProductNames();
        List<String> expectedDesc = new ArrayList<>(namesDesc);
        expectedDesc.sort(Collections.reverseOrder());
        Assert.assertEquals(namesDesc, expectedDesc, "Names should be in reverse alphabetical order");

        // Price (low to high)
        inventoryPage.sortBy("Price (low to high)");
        ExtentManager.logStep(getDriver(), "Sorted by Price (low to high)");
        List<Double> pricesAsc = inventoryPage.getProductPrices();
        List<Double> expectedPricesAsc = new ArrayList<>(pricesAsc);
        Collections.sort(expectedPricesAsc);
        Assert.assertEquals(pricesAsc, expectedPricesAsc, "Prices should be ascending");

        // Price (high to low)
        inventoryPage.sortBy("Price (high to low)");
        ExtentManager.logStep(getDriver(), "Sorted by Price (high to low)");
        List<Double> pricesDesc = inventoryPage.getProductPrices();
        List<Double> expectedPricesDesc = new ArrayList<>(pricesDesc);
        expectedPricesDesc.sort(Collections.reverseOrder());
        Assert.assertEquals(pricesDesc, expectedPricesDesc, "Prices should be descending");
    }
}
