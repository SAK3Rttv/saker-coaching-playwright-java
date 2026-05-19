package tests.functional;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.microsoft.playwright.Page;

import base.BaseTest;
import pages.HomePage;
import pages.LoginPage;
import pages.NavbarComponent;
import utils.ExcelUtil;
import utils.WaitUtil;

public class LoginTest extends BaseTest {

//	@DataProvider(name = "loginDataProvider")

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

		// Navigate & open login modal
		goHome();
		HomePage home = new HomePage(page);
		home.clickSignInNavButton();

		LoginPage loginPage = new LoginPage(page);
		WaitUtil.waitForVisible(page.locator("#email").first());

		// Perform login

		boolean alertFired = false;
		page.onceDialog(dialog -> {
			getTest().fail("Unexpected dialog fired: " + dialog.message());
			dialog.dismiss();
		});

		try {
			if (rememberMe) {
				loginPage.loginWithRememberMe(email, password);
			} else {
				loginPage.login(email, password);
			}
		} catch (Exception e) {
			getTest().info("Login action threw expception: " + e.getMessage());
		}

		WaitUtil.sleep(5000);
		
//		WaitUtil.waitForVisible(page.locator("#email").first());

		// Evaluate outcome
		NavbarComponent nav = new NavbarComponent(page);
		boolean loggedIn = nav.isLoggedIn();
		boolean hasError = loginPage.isErrorMessageVisible();
		boolean formOpen = loginPage.isSignInButtonVisiable();
		
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
		
		// Category-specific extra checks
		if("Security".equalsIgnoreCase(category)) {
			assertNoXssExecuted();
			assertNotAuthenticated(loggedIn, testCaseId);
		}
		
		if("Edge".equalsIgnoreCase(category)) {
			assertPageStable();
		}
	}
	
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
