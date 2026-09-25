package base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import utils.ConfigReader;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class BaseTest 
{
    private static final Logger log = LogManager.getLogger(BaseTest.class);
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    public static WebDriver getDriver() 
    {
        return driverThreadLocal.get();
    }

    @BeforeMethod(alwaysRun = true)
    @Parameters({"browser"})
    public void setUp(@Optional("") String browserParam) throws Exception 
    {
    	String browser = (browserParam != null && !browserParam.isBlank()) ? browserParam : ConfigReader.get("browser", "chrome");
        boolean headless = ConfigReader.getBoolean("headless", false);

        log.info("Launching browser: {} (headless={})", browser, headless);
        WebDriver driver = createDriver(browser, headless);

        driver.manage().window().maximize();
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigReader.getInt("page.load.timeout.seconds", 30)));
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);

        driverThreadLocal.set(driver);

        String baseUrl = ConfigReader.get("base.url", "https://www.saucedemo.com/");
        driver.get(baseUrl);
        log.info("Navigated to {}", baseUrl);
    }

    private WebDriver createDriver(String browser, boolean headless) 
    {   
    	switch (browser.trim().toLowerCase()) 
    	{
    	case "chrome":
    	    ChromeOptions chromeOptions = new ChromeOptions();

    	    if (headless)
    	        chromeOptions.addArguments("--headless=new");

    	    chromeOptions.addArguments("--remote-allow-origins=*", "--window-size=1920,1080");

    	    Map<String, Object> prefs = new HashMap<>();
    	    prefs.put("credentials_enable_service", false);
    	    prefs.put("profile.password_manager_enabled", false);
    	    prefs.put("profile.password_manager_leak_detection", false);

    	    chromeOptions.setExperimentalOption("prefs", prefs);

    	    return new ChromeDriver(chromeOptions);

    	case "edge":
    	    EdgeOptions edgeOptions = new EdgeOptions();

    	    if (headless)
    	        edgeOptions.addArguments("--headless=new");

    	    edgeOptions.addArguments(
    	            "--remote-allow-origins=*",
    	            "--window-size=1920,1080"
    	    );

    	    Map<String, Object> edgePrefs = new HashMap<>();
    	    edgePrefs.put("credentials_enable_service", false);
    	    edgePrefs.put("profile.password_manager_enabled", false);
    	    edgePrefs.put("profile.password_manager_leak_detection", false);

    	    edgeOptions.setExperimentalOption("prefs", edgePrefs);

    	    return new EdgeDriver(edgeOptions);

    	case "firefox":
    	    FirefoxOptions firefoxOptions = new FirefoxOptions();

    	    if (headless)
    	        firefoxOptions.addArguments("--headless");

    	    firefoxOptions.addArguments("--width=1920", "--height=1080");

    	    firefoxOptions.addPreference("signon.rememberSignons", false);
    	    firefoxOptions.addPreference("signon.management.page.breach-alerts.enabled", false);
    	    firefoxOptions.addPreference("signon.management.page.fileImport.enabled", false);

    	    return new FirefoxDriver(firefoxOptions);

    	    default : 
    	        throw new IllegalArgumentException("Unsupported browser: " + browser);
    	}
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() 
    {
        WebDriver driver = getDriver();
        if (driver == null) 
        	return;

        log.info("Closing browser for thread {}", Thread.currentThread().getId());
        driver.quit();
        driverThreadLocal.remove();
    }
}
