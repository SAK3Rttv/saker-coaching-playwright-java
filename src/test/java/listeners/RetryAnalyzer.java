package listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retry analyzer for flaky tests
 * Automatically retries failed tests up to MAX_RETRIES times
 * 
 * Usage: @Test(retryAnalyzer = RetryAnalyzer.class)
 */
public class RetryAnalyzer implements IRetryAnalyzer {
    private static final Logger log = LoggerFactory.getLogger(RetryAnalyzer.class);
    private int retryCount = 0;
    private static final int MAX_RETRIES = 2;  // Retry up to 2 times (3 attempts total)

    @Override
    public boolean retry(ITestResult result) {
        if (!result.isSuccess() && retryCount < MAX_RETRIES) {
            retryCount++;
            log.warn("Test FAILED: {} | Retry Attempt: {}/{}", 
                result.getName(), retryCount, MAX_RETRIES);
            return true;  // Retry this test
        }
        return false;  // No more retries
    }

    public int getRetryCount() {
        return retryCount;
    }
}
