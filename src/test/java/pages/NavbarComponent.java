package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class NavbarComponent {

	private final Page page;
	
	private final Locator logInNavBtn;
	
	public NavbarComponent(Page page) {
		this.page = page;
		logInNavBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login"));
	}
	
	public boolean isLoggedIn() {
		return !logInNavBtn.isVisible();
	}
	
	public boolean isSignInVisible() { return logInNavBtn.isVisible(); }
}
