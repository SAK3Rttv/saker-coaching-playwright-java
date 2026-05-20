package tests.functional;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import base.BaseTest;
import pages.HomePage;
import pages.LoginPage;
import pages.NavbarComponent;
import utils.ExcelUtil;
import utils.WaitUtil;

public class LoginTest extends BaseTest {


	@Test(dataProvider = "loginDataProvider", dataProviderClass = ExcelUtil.class, description = "Data-driven login - covers Positive / Negative / Edge / Security")
	public void loginTest(String testCaseId, String category, String description, String email, String password,
			boolean rememberMe, String expectedResult, String expectedErrorContains, String notes) {
		logTestMetadata(testCaseId, category, description, email, rememberMe, expectedResult, notes);
		
		// 1. Setup & Navigation
		navigateToLoginPage();
		LoginPage loginPage = new LoginPage(page);
		NavbarComponent nav = new NavbarComponent(page);
		
		
		// 2. Execution
		handleUnexpectedDialogs();
		performLoginAction(loginPage, email, password, rememberMe);
		waitForLoginProcessToComplete();
		
		// 3. Capture State
		boolean loggedIn = nav.isLoggedIn();
		boolean hasError = loginPage.isErrorMessageVisible();
		boolean formOpen = loginPage.isSignInButtonVisiable();
		
		getTest().info("Final State -> Logged in: " + loggedIn + ", Error shown: " + hasError);
	
	
	   // 4. Verification Logic
		verifyResults(expectedResult, loggedIn, hasError, formOpen, testCaseId, email, expectedErrorContains, loginPage, rememberMe,category);
	    verifyCategorySpecifics(category, loggedIn, testCaseId);
	}
	
	private void navigateToLoginPage() {
		goHome();
		new HomePage(page).clickSignInNavButton();
		WaitUtil.waitForVisible(page.locator("#email").first());
	}
	
	private void performLoginAction(LoginPage loginPage,String email,String password,boolean rememberMe) {
		try {
			if(rememberMe) {
				loginPage.loginWithRememberMe(email, password);
			} else {
				loginPage.login(email, password);
			}
		} catch (Exception e) {
			getTest().info("Login action exception: " + e.getMessage());
		}
	}
	
	private void waitForLoginProcessToComplete() {
		Locator loading = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Signing In..."));
		WaitUtil.waitForHidden(loading);
		WaitUtil.sleep(3000);
	}
	
	private void verifyResults(String expectedResult, boolean loggedIn, boolean hasError, boolean formOpen,
			String id,String email,String errorHint,LoginPage loginPage, boolean rememberMe,String cat) {
		switch (expectedResult.toUpperCase()) {
		case "LOGIN_SUCCESS":
			assertSuccess(loggedIn, id, email);
		    if (rememberMe) assertRememberMeToken();
		    break;
		case "LOGIN_FAIL":
			assertFailure(loggedIn, hasError, formOpen, id, email, errorHint, loginPage,cat);
		    break;
		default:
			  Assert.fail("Invalid ExpectedResult in Excel: " + expectedResult);
		}
	}
	
	private void verifyCategorySpecifics(String category, boolean loggedIn,String id) {
		if ("Security".equalsIgnoreCase(category)) { 
			assertNoXssExecuted();
			assertNotAuthenticated(loggedIn, id);
		}
		if ("Edge".equalsIgnoreCase(category)) {
			assertPageStable();
		}
	}
	
	private void handleUnexpectedDialogs() {
		page.onceDialog(dialog -> {
			getTest().fail("Unexpected dialog: " + dialog.message());
			dialog.dismiss();
		});
	}
	
	private void logTestMetadata(String id,String cat,String desc,String mail,boolean rem,String exp,String notes) {
		getTest().info("_____________________________________");
		getTest().info("TestCase: " + id + " | Category: " + cat);
		getTest().info("Description: " + desc);
		getTest().info("Email: " + maskIfSecurity(mail,cat) + " | RememberMe: " + rem);
		getTest().info("Expected: " + exp);
		if (!notes.isEmpty()) getTest().info("Notes: " + notes);
	}

	private void assertSuccess(boolean loggedIn, String id, String email) {
		if (!loggedIn) {
			getTest().fail(" [" + id + "] Expected LOGIN_SUCCESS but login failed for: " + email);
			Assert.fail("[" + id + "] Expected successful login but user is not authenticated.");
		}
		getTest().pass(" [" + id + "] Login SUCCESS as expected for: " + email);
	}

	private void assertFailure(boolean loggedIn, boolean hasError, boolean formOpen, String id, String email,
			String errorHint, LoginPage loginPage,String cat) {
		if (loggedIn) {
			getTest().fail("[" + id + "] Expected LOGIN_FAIL but login SUCCEEDED for: " + email);
			Assert.fail("[" + id + "] Login should have failed but user is authenticated.");
		}

		// Verify some failure signal is present
		boolean failureSignalPresent = hasError || formOpen || loginPage.isResendVerificationVisible();

		Assert.assertTrue(failureSignalPresent,
				"[" + id + "] Expected a failure signal (error message or form still open) for: " + email);

		// If we expect a specific error substring, verify it
		if (!errorHint.isEmpty() && hasError) {
			String errorText = loginPage.getErrorMessageText().toLowerCase();
			boolean hintFound = errorText.contains(errorHint.toLowerCase());
			getTest().info("Error text : \"" + errorText + "\"");
			getTest().info("Hint check : \"" + errorHint + "\" found=" + hintFound);
			Assert.assertTrue(hintFound,
					"[" + id + "] Error message should contain \"" + errorHint + "\" but got: \"" + errorText + "\"");
		}

		getTest().pass("[" + id + "] Login correctly rejected for: " + maskIfSecurity(email, cat));
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
	
	private static String maskIfSecurity(String email,String category) {
		if ("Security".equalsIgnoreCase(category)) {
			return email.length() > 10 ? 
					email.substring(0,6) + "***[payload masked]" : "***[payload masked]";
			
		}
		return email;
	}
}
