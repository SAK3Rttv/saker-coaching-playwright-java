package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import utils.WaitUtil;

/**
 * Page object for the Login modal overlay on SAK3R Coaching.
 * 
 * The modal is triggered from the homepage and is NOT a routable URL.
 */
public class LoginPage {

	private final Page page;

	/*
	 * Inputs
	 */
	private final Locator emailInput;
	private final Locator passwordInput;
	private final Locator rememberMeCheckbox;
	private final Locator SignInButton;

	/*
	 * Error / feedback
	 */
	private final Locator errorMessage;
	private final Locator successToast;
	private final Locator resendVerificationBtn;
	private final Locator emptyField;

	public LoginPage(Page page) {
		this.page = page;

		emailInput = page.locator("#email").first();
		passwordInput = page.locator("#password").first();
		
		
//		rememberMeCheckbox = page.getByLabel("Remember me", new Page.GetByLabelOptions().setExact(true));
//		SignInButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign In"));
		rememberMeCheckbox = page.locator("label:has-text('Remember me') input, input[type='checkbox']");
		SignInButton = page.locator("button:has-text('Sign In')");
		
		
		
//		errorMessage = page.getByRole(AriaRole.LISTITEM).filter(new Locator.FilterOptions().setHasText("Invalid credentials"));		
//		successToast = page.locator(".toast-success, .Toastify__toast--success").first();
//		resendVerificationBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Resend"));
//		emptyField = page.getByText("Please fill out this field.");
		this.errorMessage = page.locator("li:has-text('Invalid credentials')");
	    this.successToast = page.locator(".toast-success, .Toastify__toast--success").first();
	    this.resendVerificationBtn = page.locator("button:has-text('Resend')");
	    this.emptyField = page.locator(":text('Please fill out this field.')");
	}

	/*
	 * Actions
	 */

	public void enterEmail(String email) {
		emailInput.clear();
		emailInput.fill(email);
	}

	public void enterPassword(String password) {
		passwordInput.clear();
		passwordInput.fill(password);
	}

	public void checkRememberMe() {
		if (!rememberMeCheckbox.isChecked())
			rememberMeCheckbox.check();
	}

	public void clickSignIn() {
		SignInButton.click();
	}

	/* High-level compound actions */

	/**
	 * Full login flow: fill email + password + click Sign In. Does NOT check
	 * "Remember me" unless explicitly called.
	 */

	public void login(String email, String password) {
//		WaitUtil.waitForVisible(emailInput);
		enterEmail(email);
		enterPassword(password);
		clickSignIn();
	}

	public void loginWithRememberMe(String email, String password) {
//		WaitUtil.waitForVisible(emailInput);
		enterEmail(email);
		enterPassword(password);
		checkRememberMe();
		clickSignIn();
	}

	/* Verification helpers */

	public boolean isSignInButtonVisiable() {
		return SignInButton.isVisible();
	}

	public boolean isRememberMeChecked() {
		return rememberMeCheckbox.isChecked();
	}
	
	public boolean isErrorMessageVisible() {
//		try {
//			return errorMessage.count() > 0 && errorMessage.isVisible();
//		} catch (Exception e) {
//			return false;
//		}
//		Locator test = page.getByRole(AriaRole.LISTITEM).filter(new Locator.FilterOptions().setHasText("No refresh token"));
//		System.out.println(test.ariaSnapshot());
//		System.out.println(test.innerHTML());
		return errorMessage.isVisible();
	}
	
	public String getErrorMessageText() {
		if (!errorMessage.isVisible()) {
			return "not";
		}
//		return "done";
		System.out.println("errorMessage.ariaSnapshot() "  + errorMessage.ariaSnapshot());
		System.out.println("errorMessage.innerHTML() "  + errorMessage.innerHTML());
		System.out.println("errorMessage.textContent() "  + errorMessage.textContent());
		System.out.println("errorMessage.textContent().trim() "  + errorMessage.textContent().trim());
		return errorMessage.textContent().trim();
	}
	
	public boolean isResendVerificationVisible() {
		return resendVerificationBtn.isVisible();
	}
	
	public boolean isThereEmptyField() {
		return emptyField.isVisible();
	}
	
}
