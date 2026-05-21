package tests.functional;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import base.BaseTest;
import config.ConfigReader;
import listeners.TestListener;
import pages.HomePage;
import pages.LoginPage;
import pages.NavbarComponent;

@Listeners(TestListener.class)
public class AuthTests extends BaseTest {
	
	
	@Test(description = "FN-AUTH | Successful login updates navbar to authenticated state")
	public void loginUpdatedNavbar() {
		goHome();
		HomePage home = new HomePage(page);
		home.clickSignInNavButton();
		
		getTest().info("Logging in with valid user credentails");
		new LoginPage(page).login(
				ConfigReader.get("user.email"),
				ConfigReader.get("user.password")
				);
		page.waitForCondition(() -> !home.isSignInBtnVisible(),
				new com.microsoft.playwright.Page.WaitForConditionOptions().setTimeout(10000));
		
		NavbarComponent nav = new NavbarComponent(page);
		Assert.assertTrue(nav.isLoggedIn(), "Navbar should reflect authenticated state");
		getTest().pass("Navbar correctly shows authenticatd state");
	}
}
