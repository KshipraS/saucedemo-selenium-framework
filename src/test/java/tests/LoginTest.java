package tests;

import base.BaseTest;
import dataproviders.ExcelDataProvider;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.InventoryPage;
import pages.LoginPage;
import utils.ExtentManager;

import java.util.Map;

public class LoginTest extends BaseTest {

    @Test(dataProvider = "loginData", dataProviderClass = ExcelDataProvider.class,
          description = "Login with valid, invalid, locked-out, and empty credentials")
    public void loginTest(Map<String, String> row) {

        String testCaseId = row.get("TestCaseId");
        String username = row.get("Username");
        String password = row.get("Password");
        String expectedResult = row.get("ExpectedResult");
        String expectedMessage = row.get("ExpectedMessage");

        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.enterUsername(username).enterPassword(password);
        ExtentManager.logStep(getDriver(), "Entered credentials for [" + testCaseId + "]");

        if ("SUCCESS".equalsIgnoreCase(expectedResult)) {
            InventoryPage inventoryPage = loginPage.clickLogin();
            ExtentManager.logStep(getDriver(), "Login successful - redirected to Products page");
            Assert.assertEquals(inventoryPage.getPageHeading(), "Products",
                    "[" + testCaseId + "] Expected redirect to Products page after valid login");
        } else {
            loginPage.clickLoginExpectingFailure();
            String actualMessage = loginPage.getErrorMessage();
            ExtentManager.logStep(getDriver(), "Login rejected - error displayed: " + actualMessage);
            Assert.assertEquals(actualMessage, expectedMessage,
                    "[" + testCaseId + "] Error message did not match expected text");
        }
    }
}