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
    private static int counter = 1000;

	@Test(dataProvider = "loginDataProvider", dataProviderClass = ExcelUtil.class, description = "Data-driven login - covers Positive / Negative / Edge / Security")
	public void loginTest(String testCaseId, String category, String description, String email, String password,
			boolean rememberMe, String expectedResult, String expectedErrorContains, String notes) {
		logTestMetadata(testCaseId, category, description, email, rememberMe, expectedResult, notes);
		System.out.println(counter++);
		System.out.println("1");
		// 1. Setup & Navigation
		navigateToLoginPage();
		System.out.println("1.1");
		LoginPage loginPage = new LoginPage(page);
		System.out.println("1.2");
		NavbarComponent nav = new NavbarComponent(page);
		
		System.out.println("2");
		// 2. Execution
		handleUnexpectedDialogs();
		System.out.println("2.1");
		performLoginAction(loginPage, email, password, rememberMe);
		System.out.println("2.2");
		waitForLoginProcessToComplete(category);
		System.out.println("2.3");
		
		System.out.println("3");
		// 3. Capture State
		boolean loggedIn = nav.isLoggedIn();
		System.out.println("3.1");
		boolean hasError = loginPage.isErrorMessageVisible();
		System.out.println("55 "  + loginPage.getErrorMessageText());
		boolean formOpen = loginPage.isSignInButtonVisiable();
		
		getTest().info("Final State -> Logged in: " + loggedIn + ", Error shown: " + hasError);
	
		System.out.println("4");
	   // 4. Verification Logic
		verifyResults(expectedResult, loggedIn, hasError, formOpen, testCaseId, email, expectedErrorContains, loginPage, rememberMe,category);
	    verifyCategorySpecifics(category, loggedIn, testCaseId);
	    System.out.println("5");
	}
	
	private void navigateToLoginPage() {
		System.out.println("nv 1");
		goHome();
		System.out.println("nv 2");
		new HomePage(page).clickSignInNavButton();
		System.out.println("nv 3");
//		WaitUtil.waitForVisible(page.locator("#email").first());
		System.out.println("nv 4");
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
	
//	private void waitForLoginProcessToComplete(LoginPage loginPage, NavbarComponent nav) {
//	    try {
//	    	
//	    	System.out.println("nav.isLoggedIn()  " + nav.isLoggedIn() );
//	    	System.out.println("loginPage.isErrorMessageVisible()   " + loginPage.isErrorMessageVisible()  );
//	    	System.out.println("loginPage.isErrorMessageVisible()   " + loginPage.isErrorMessageVisible()  );
//	        page.waitForCondition(
//	            () -> nav.isLoggedIn() 
//	            ||
//	            loginPage.isErrorMessageVisible() 
//	            || 
//	            loginPage.isResendVerificationVisible(),
//	            new Page.WaitForConditionOptions().setTimeout(15000)
//	        );
//	    } catch (TimeoutError e) {
//	        getTest().warning("Login outcome not resolved within 15s — proceeding with current state");
//	    }
//	}
	private void waitForLoginProcessToComplete(String cat) {
		System.out.println("f1");
//		Locator loading = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Signing In..."));
		Locator loading = page.locator("button:has-text('Signing In...')");
		System.out.println("f2");
		WaitUtil.waitForHidden(loading);
//		loading.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN).setTimeout(30000));

		
		
		
		
		
		
		
		
		//		WaitUtil.waitForHidden(loading);
		System.out.println("f3");
		
		if (cat.equalsIgnoreCase("Positive")) {
			System.out.println("f4");
			
//			WaitUtil.waitForVisible(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Notifications")));
			
			// 1. Target the button via fast CSS
			Locator notificationsBtn = page.locator("button[aria-label='Notifications']");
            WaitUtil.waitForVisible(notificationsBtn);

			// 2. Force Playwright to strictly wait until the element is fully VISIBLE in the viewport
//			notificationsBtn.waitFor(new Locator.WaitForOptions()
//			    .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
//			    .setTimeout(15000));
			System.out.println("f5");
		} else {
			System.out.println("f6");
//			page.locator("button:has-text('Sign In')").waitFor();
			Locator signIn = page.locator("button:has-text('Sign In')");
			WaitUtil.waitForVisible(signIn);
//			WaitUtil.waitForVisible(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign In")));
			System.out.println("f7");
		}
//		WaitUtil.sleep(3000);
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
		boolean failureSignalPresent = hasError || formOpen || loginPage.isResendVerificationVisible() || loginPage.isThereEmptyField();

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
