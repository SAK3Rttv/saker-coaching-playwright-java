package base;

import java.lang.reflect.Method;

import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.aventstack.extentreports.ExtentTest;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import config.ConfigReader;
import listeners.TestListener;

/**
 * Base class for all SAK3R test classes.
 * 
 * Playwright setup/teardown is handled here. ExtentReports is wired via
 * {@link TestListener} - use {@code getTest()} in subclasses to log steps
 */
public class BaseTest {

	protected Playwright playwright;
	public Browser browser;
	public Page page;

	// Setup
	@BeforeMethod(alwaysRun = true)
	public void setUp(Method method) {
		playwright = Playwright.create();

		String browserName = ConfigReader.get("browser.name").toLowerCase();
		boolean headless = ConfigReader.getBool("browser.headless");
		int slowMo = ConfigReader.getInt("browser.slow.mo");

		BrowserType.LaunchOptions launchOpts = new BrowserType.LaunchOptions().setHeadless(headless).setSlowMo(slowMo);

		browser = switch (browserName) {
		case "firefox" -> playwright.firefox().launch(launchOpts);
		case "webkit" -> playwright.webkit().launch(launchOpts);
		default -> playwright.chromium().launch(launchOpts);
		};

		Browser.NewContextOptions ctxOpts = new Browser.NewContextOptions().setViewportSize(1440, 900);

		BrowserContext context = browser.newContext(ctxOpts);
		page = context.newPage();
		page.setDefaultTimeout(ConfigReader.getInt("timeout.default"));
		page.setDefaultNavigationTimeout(ConfigReader.getInt("timeout.navigation"));

//		getTest().info("Test Started - Browser: " + browserName); give error null bcz not init
	}

	// Teardown
	@AfterMethod(alwaysRun = true)
	public void tearDown(ITestResult result) {
		if (browser != null)
			browser.close();
		if (playwright != null)
			playwright.close();
	}

	// Helpers

	/**
	 * Convenience accessor for the current thread's ExtendTest.
	 * 
	 * @return
	 */
	protected ExtentTest getTest() {
		return TestListener.getTest();
	}

	/**
	 * Navigate to the base URL.
	 */
	protected void goHome() {
		page.navigate(ConfigReader.get("base.url"), new Page.NavigateOptions()
				.setWaitUntil(com.microsoft.playwright.options.WaitUntilState.DOMCONTENTLOADED));
	}
}
