package listeners;

import java.util.Base64;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;

import base.BaseTest;
import utils.ExtentManager;

/**
 * Registered in testng.xml under <listeners>. Creates one ExtentTest node per
 * @Test method and logs pass/fail/skip automatically - no manual
 * extentTest.log(...) calls scattered through the test classes themselves.
 *
 * Screenshot capture lives here, not in BaseTest's @AfterMethod. TestNG calls
 * onTestFailure() right after the failed @Test method returns, and only
 * afterwards runs @AfterMethod/@AfterClass configuration methods - so at the
 * point onTestFailure() fires, BaseTest.getDriver() is still a live browser
 * session (driver.quit() hasn't happened yet). Capturing here, instead of in
 * @AfterMethod and reading it back out via an ITestResult attribute, is what
 * actually gets a non-empty screenshot into the report.
 */
public class TestListener implements ITestListener 
{
    private static final Logger log = LogManager.getLogger(TestListener.class);

    @Override
    public void onTestStart(ITestResult result) 
    {
        String className = result.getTestClass().getRealClass().getSimpleName();
        ExtentTest test = ExtentManager.createTest(className + "." + result.getMethod().getMethodName(),result.getMethod().getDescription());
        test.assignCategory(className);
        log.info("Starting test: {}", result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) 
    {
        ExtentTest test = ExtentManager.getTest();
        if (test != null) 
        {
            test.log(Status.PASS, "Test passed");
            attachScreenshot(test, result);
        } else 
            log.warn("No active ExtentTest for {} - skipping report log", result.getMethod().getMethodName());
        
        log.info("PASSED: {}", result.getMethod().getMethodName());
        ExtentManager.removeTest();
    }

    @Override
    public void onTestFailure(ITestResult result) 
    {
        ExtentTest test = ExtentManager.getTest();
        if (test != null) 
        {
	        test.log(Status.FAIL, result.getThrowable());
	        attachScreenshot(test, result);
        }
        log.error("FAILED: {}", result.getMethod().getMethodName(), result.getThrowable());
        ExtentManager.removeTest();
    }

    private void attachScreenshot(ExtentTest test, ITestResult result) 
    {

        WebDriver driver = BaseTest.getDriver();

        if (driver == null) 
        {
            log.warn("No active driver - cannot capture screenshot");
            return;
        }
        try 
        {
            byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

            String base64 = Base64.getEncoder().encodeToString(screenshotBytes);

            if (result.getStatus() == ITestResult.FAILURE) 
                test.fail("Screenshot on failure", MediaEntityBuilder.createScreenCaptureFromBase64String(base64).build());
            else 
                test.info("Screenshot", MediaEntityBuilder.createScreenCaptureFromBase64String(base64).build());
         } catch (Throwable e) {
        	 	log.error("Failed to capture screenshot", e);
           }
    }
    
    @Override
    public void onTestSkipped(ITestResult result) 
    {
        ExtentTest test = ExtentManager.getTest();
        if (test != null) 
        {
	        test.log(Status.SKIP, "Test skipped");
	        attachScreenshot(test, result);
        }
        log.warn("SKIPPED: {}", result.getMethod().getMethodName());
        ExtentManager.removeTest();
    }

    @Override
    public void onFinish(ITestContext context) 
    {
        ExtentManager.flush();
        log.info("Extent report written to: {}", ExtentManager.getReportPath());
    }
}
