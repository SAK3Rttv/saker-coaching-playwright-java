package utils;

import java.io.File;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.microsoft.playwright.Page;

import config.ConfigReader;

/**
 * Capture full-page screenshots and return the relative path used by
 * ExtentReports (relative to the report HTML file location)
 */
public class ScreenshotUtil {

	public static String takeScreenshot(Page page, String testName) {
		String dir = ConfigReader.get("screenshots.dir");
		new File(dir).mkdirs();

		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmsss").format(new Date());
		String filename = testName.replaceAll("[^a-zA-Z0-9_-]", "_") + "_" + timestamp + ".png";
		String fullPath = dir + "/" + filename;

		try {
			page.screenshot(
					new Page.ScreenshotOptions().setPath(Paths.get(fullPath)).setFullPage(true).setTimeout(3000));
		} catch (Exception e) {
			System.err.println("[ScreenshotUtil] Failed to capture screenshot: " + e.getMessage());
			return null;
		}

		// Return path relative to report HTML (one level up from test-output/)
		return "screenshots/" + filename;
	}
}
