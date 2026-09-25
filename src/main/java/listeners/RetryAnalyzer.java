package listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import utils.ConfigReader;

/**
 * Retries a failed test up to maxRetryCount times (config: retry.count,
 * default 1 - i.e. one retry, two attempts total). Applied to every @Test
 * automatically via RetryTransformer, so nobody has to remember to add
 * retryAnalyzer = RetryAnalyzer.class to each method.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger log = LogManager.getLogger(RetryAnalyzer.class);
    private int retryCount = 0;
    private final int maxRetryCount = ConfigReader.getInt("retry.count", 1);	// 1 is default

    @Override
    public boolean retry(ITestResult result) 
    {
        if (retryCount < maxRetryCount) 
        {
            retryCount++;
            log.warn("Retrying '{}' - attempt {} of {}", result.getName(), retryCount, maxRetryCount);
            return true;
        }
        return false;
    }
}
