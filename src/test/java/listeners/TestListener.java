package listeners;

import java.util.HashMap;
import java.util.Map;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.microsoft.playwright.Page;

import utils.ExtentManager;
import utils.ScreenshotUtil;

/**
 * TestNG listener that wires ExtentReports into every test without requiring
 * BaseTest to hold ExtentTest state.
 * 
 * Test classes that need to log steps should use the static {@link #getTest()}
 * helper.
 */
public class TestListener implements ITestListener {

	private static final ExtentReports extent = ExtentManager.getInstance();

	// thread-safe map so parallel tests don't collide
	private static final Map<Long, ExtentTest> testMap = new HashMap<>();

	public static ExtentTest getTest() {
		return testMap.get(Thread.currentThread().threadId());
	}

	@Override
	public void onTestStart(ITestResult result) {
		String testName = result.getTestClass().getRealClass().getSimpleName() + " - "
				+ result.getMethod().getMethodName();
		ExtentTest test = extent.createTest(testName);
		test.assignCategory(result.getTestClass().getRealClass().getSimpleName());
		testMap.put(Thread.currentThread().threadId(), test);
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		getTest().pass("PASSED");
		flush();
	}

	@Override
	public void onTestFailure(ITestResult result) {
		ExtentTest test = getTest();
		test.fail(result.getThrowable());

		// Attempt screenshot via BaseTest's page reference
		Object instance = result.getInstance();
		if (instance instanceof base.BaseTest) {
			Page page = ((base.BaseTest) instance).page;
			if (page != null) {
				String path = ScreenshotUtil.takeScreenshot(page, result.getName());
				if (path != null) {
					try {
						test.addScreenCaptureFromPath("./" + path);
					} catch (Exception ignored) {
						test.info("Screenshot saved: " + path);
					}
				}
			}
		}
		flush();
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		getTest().skip("SKIPPED - " + (result.getThrowable() != null ? result.getThrowable().getMessage() : ""));
		flush();
	}

	@Override
	public void onFinish(ITestContext context) {
		flush();
	}

	private void flush() {
		extent.flush();
	}
}
