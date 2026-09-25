package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * One ExtentReports instance for the whole suite (all threads write into the
 * same HTML file); one ExtentTest per test METHOD via ThreadLocal, so
 * parallel classes/methods don't cross-log into each other's node in the
 * report — each thread only ever sees its own current ExtentTest.
 */
public class ExtentManager {

    private static final Logger log = LogManager.getLogger(ExtentManager.class);

    private static ExtentReports extent;
   // private static final Map<Long, ExtentTest> testMap = new ConcurrentHashMap<>();	// In place of Thread local
    private static final ThreadLocal<ExtentTest> testThreadLocal = new ThreadLocal<>();
    private static String reportPath;

    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String reportDir = ConfigReader.get("report.dir", "test-output/extent-reports");
            new File(reportDir).mkdirs();
            reportPath = reportDir + File.separator + "SauceDemo_Report_" + timestamp + ".html";

            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
            sparkReporter.config().setTheme(Theme.STANDARD);
            sparkReporter.config().setDocumentTitle("SauceDemo Automation Report");
            sparkReporter.config().setReportName("Selenium + TestNG Execution Report");

            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);
            extent.setSystemInfo("Application", "https://www.saucedemo.com/");
            extent.setSystemInfo("Environment", ConfigReader.get("env", "QA"));
            extent.setSystemInfo("Browser", ConfigReader.get("browser", "chrome"));
        }
        return extent;
    }

    public static String getReportPath() {
        return reportPath;
    }

    public static ExtentTest createTest(String testName, String description) {
        ExtentTest test = getInstance().createTest(testName, description);
        //testMap.put(Thread.currentThread().getId(), test);
        testThreadLocal.set(test);
        return test;
    }

    public static ExtentTest getTest() {
        //return testMap.get(Thread.currentThread().getId());
    	return  testThreadLocal.get();
    }

    public static void logStep(WebDriver driver, String description) {
        ExtentTest test = getTest();
        if (test == null) {
            log.warn("No active ExtentTest for thread {} - cannot log step: {}",
                    Thread.currentThread().getId(), description);
            return;
        }
        if (driver == null) {
            test.info(description); // still record the step text even without a screenshot
            return;
        }
        try {
            byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            String base64 = Base64.getEncoder().encodeToString(screenshotBytes);
            test.info(description, MediaEntityBuilder.createScreenCaptureFromBase64String(base64).build());
        } catch (Throwable e) {
            log.error("Failed to capture screenshot for step: {}", description, e);
            test.info(description); // fall back to a text-only step so the trail isn't missing an entry
        }
    }

    public static void removeTest() {
        testThreadLocal.remove(); // clears this thread's slot after the test finishes
    }
    
    public static synchronized void flush() {
        if (extent != null) {
            extent.flush();
        }
    }
}