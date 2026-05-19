package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitUntilState;

import config.ConfigReader;

/**
 * Page object for the SAK3R Coaching landing page (/)
 * 
 * Most "pages" in SAK3R are modal overlays rendered on top of this route, so
 * this class also contains the navbar controls that open those overlays.
 */
public class HomePage {

	private final Page page;

	// Navbar
	private final Locator logInNavBtn;

	public HomePage(Page page) {
		this.page = page;

		// Navbar
		logInNavBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login"));

	}

	/* Navigation */
	public void navigate() {
		page.navigate(ConfigReader.get("base.url"),
				new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE)
				.setTimeout(30000));
	}

	/*
	 * Navbar actions
	 */
	public void clickSignInNavButton() {
		logInNavBtn.click();
	}

	/*
	 * Verification helpers
	 */

	public boolean isSignInBtnVisible() {
		return logInNavBtn.isVisible();
	}

	public String getTitle() {
		return page.title();
	}
}
