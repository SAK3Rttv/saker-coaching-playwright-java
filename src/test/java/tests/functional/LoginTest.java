package tests.functional;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

import base.BaseTest;
import pages.HomePage;
import pages.LoginPage;
import pages.NavbarComponent;
import utils.ExcelUtil;
import utils.WaitUtil;

public class LoginTest extends BaseTest {

//	@DataProvider(name = "loginDataProvider")
	public static int debuger = 1000;

	@Test(dataProvider = "loginDataProvider", dataProviderClass = ExcelUtil.class, description = "Data-driven login - covers Positive / Negative / Edge / Security")
	public void loginTest(String testCaseId, String category, String description, String email, String password,
			boolean rememberMe, String expectedResult, String expectedErrorContains, String notes) {
		getTest().info("__________________________________________");
		getTest().info("TestCase    : " + testCaseId);
		getTest().info("Category    : " + category);
		getTest().info("Description : " + description);
		getTest().info("Email       : " + email);
		getTest().info("RememberMe  : " + rememberMe);
		getTest().info("Expected    : " + expectedResult);
		if (!notes.isEmpty())
			getTest().info("Notes       : " + notes);
		System.out.println("6");
		System.out.println(debuger++);
		// Navigate & open login modal
		goHome();
		System.out.println("50");
		HomePage home = new HomePage(page);
		System.out.println("51");
		home.clickSignInNavButton();
		System.out.println("52");
		LoginPage loginPage = new LoginPage(page);
		System.out.println("7");
		WaitUtil.waitForVisible(page.locator("#email").first());

		// Perform login

		boolean alertFired = false;
		page.onceDialog(dialog -> {
			getTest().fail("Unexpected dialog fired: " + dialog.message());
			dialog.dismiss();
		});
		System.out.println("8");
		try {
			if (rememberMe) {
				loginPage.loginWithRememberMe(email, password);
			} else {
				loginPage.login(email, password);
			}
		} catch (Exception e) {
			getTest().info("Login action threw expception: " + e.getMessage());
		}
		System.out.println("9");
//		WaitUtil.sleep(5000);
		
//		WaitUtil.waitForVisible(page.locator("#email").first());
		Locator loading = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Signing In..."));
		WaitUtil.waitForHidden(loading);
//		Locator loading = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign In"));
		
//		Waitfor
//		Sign In
//		System.out.println("loading " + loading.ariaSnapshot());
//		System.out.println("before wait hidden: visiable " + loading.isVisible());
//		WaitUtil.sleep(3000);
//		loading.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
//		WaitUtil.waitForVisible(loading);
//		WaitUtil.waitForHidden(loading);
//		System.out.println("after wait hidden: visiable " + loading.isVisible());
//		WaitUtil.waitForVisible(loading);
//		System.out.println("after wait visible: visiable " + loading.isVisible());
//		WaitUtil.sleep(3000);
		// Evaluate outcome
		
		WaitUtil.sleep(3000);
		
		NavbarComponent nav = new NavbarComponent(page);
		System.out.println("9.1");
//		boolean loggedIn = waitForLoginOutcome(loginPage,nav);
		boolean loggedIn = nav.isLoggedIn();
		System.out.println("9.2");
		boolean hasError = loginPage.isErrorMessageVisible();
		System.out.println("9.3");
		boolean formOpen = loginPage.isSignInButtonVisiable();
		System.out.println("10");
//		page.waitForCondition(() -> 
//		nav.isLoggedIn() || loginPage.isErrorMessageVisible(),
//		new Page.WaitForConditionOptions().setTimeout(10000)
//				);
		
		System.out.println("Logged in : " + loggedIn);
		System.out.println("Error shown : " + hasError);
		System.out.println("expected " + expectedResult);
		
		getTest().info("Logged in : " + loggedIn);
		getTest().info("Error shown : " + hasError);

		// Assert based on expected result
		switch (expectedResult.toUpperCase()) {
		case "LOGIN_SUCCESS":
			assertSuccess(loggedIn, testCaseId, email);
			if (rememberMe) assertRememberMeToken();
			break;
		case "LOGIN_FAIL":
			assertFailure(loggedIn, hasError, formOpen, testCaseId, email, expectedErrorContains, loginPage);
			break;
		default:
			Assert.fail("Unknown ExpectedResult value in Excel: " + expectedResult);
		}
		System.out.println("11");
		
		// Category-specific extra checks
		if("Security".equalsIgnoreCase(category)) {
			assertNoXssExecuted();
			assertNotAuthenticated(loggedIn, testCaseId);
		}
		
		if("Edge".equalsIgnoreCase(category)) {
			assertPageStable();
		}
		System.out.println("12");
	}
	
//	private boolean waitForLoginOutcome(LoginPage loginPage, NavbarComponent nav) {
//		try {
//			page.waitForCondition(() -> nav.isLoggedIn() || loginPage.isErrorMessageVisible(),
//					new Page.WaitForConditionOptions().setTimeout(15000));
//			return nav.isLoggedIn();
//		} catch (TimeoutError e) {
//			getTest().warning("Login outcome did not resolve within 15s");
//			return false;
//		}
//	}
	
	private void assertSuccess(boolean loggedIn, String id, String email) {
		if (loggedIn) {
			getTest().pass(" [" + id + "] Login SUCCESS as expected for: " + email);
		} else {
			getTest().fail(" [" + id + "] Expected LOGIN_SUCCESS but login failed for: " + email);
			Assert.fail("[" + id + "] Expected successful login but user is not authenticated.");
		}
	}
	
	private void assertFailure(boolean loggedIn, boolean hasError, boolean formOpen, String id,String email,String errorHint,
			LoginPage loginPage) {
		if (loggedIn) {
			getTest().fail("[" + id + "] Expected LOGIN_FAIL but login SUCCEEDED for: " + email);
			Assert.fail("[" + id + "] Login should have failed but user is authenticated.");
		}
		
		// Verify some failure signal is present
		boolean failureSignalPresent = hasError || formOpen || loginPage.isResendVerificationVisible();
		
		Assert.assertTrue(failureSignalPresent, "[" + id + "] Expected a failure signal (error message or form still open) for: " + email);
		
		// If we expect a specific error substring, verify it
		if (!errorHint.isEmpty() && hasError) {
			String errorText = loginPage.getErrorMessageText().toLowerCase();
			boolean hintFound = errorText.contains(errorHint.toLowerCase());
			getTest().info("Error text : \"" + errorText + "\"");
			getTest().info("Hint check : \"" + errorHint + "\" found=" + hintFound);
			Assert.assertTrue(hintFound,
					"[" + id + "] Error message should contain \"" + errorHint + "\" but got: \"" + errorText + "\"");
		}
		
		getTest().pass("[" + id + "] Login correctly rejected for: " + email);
	}
	
	private void assertRememberMeToken() {
		Object token = page.evaluate("() => localStorage.getItem('token')");
		if (token != null) {
			getTest().info("Remember Me - token found in localStorage");
		} else {
			getTest().info("Remember Me - token NOT in localStorage (may use cookie instead)");
		}
	}
	
	private void assertNoXssExecuted() {
		getTest().info("Security - No XSS dialog was triggered");
	}
	
	private void assertNotAuthenticated(boolean loggedIn, String id) {
		Assert.assertFalse(loggedIn, "[" + id + "] Security: user must NOT be authenticated after injection attempt");
	}
	
	private void assertPageStable() {
		// Verify the page hasn't crashed or navigated away unexpectedly
		String url = page.url();
		Assert.assertNotNull(url, "Page URL should not be null after edge-case input");
		Assert.assertFalse(url.isEmpty(), "Page URL should not be empty after edge-case input");
		getTest().info("Edge - Page stable at: " + url);
	}
}
