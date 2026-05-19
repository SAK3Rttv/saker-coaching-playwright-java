package utils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

import config.ConfigReader;

/**
 * Centralises common wait/assertion helpers to keep page objects clean.
 */
public class WaitUtil {

	private static final double DEFAULT_TIMEOUT = ConfigReader.getInt("timeout.element");

	/** Wait for a locator to be visible. */
	public static void waitForVisible(Locator locator) {
		locator.waitFor(
				new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(DEFAULT_TIMEOUT));
	}

	/** Wait for a locator to be hidden / detached. */
	public static void waitForHidden(Locator locator) {
		locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN).setTimeout(DEFAULT_TIMEOUT));
	}

	/** Hard pause - use sparingly, prefer explicit waits. */
	public static void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ignored) {
		}
	}

	/** Wait for any network activity to die down. */
	public static void waitForNetworkIdle(Page page) {
		page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
	}

}
