package utils;

import java.io.File;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import config.ConfigReader;

/**
 * Thread-safe singleton for ExtentReports. Creates the HTML report at
 * test-output/SAK3R-ExtentReport.html
 */
public class ExtentManager {

	private static ExtentReports extent;

	private ExtentManager() {
	}

	public static synchronized ExtentReports getInstance() {
		if (extent == null) {
			String reportDir = ConfigReader.get("report.output.dir");
			String reportName = ConfigReader.get("report.html.name");

			new File(reportDir).mkdirs();

			ExtentSparkReporter reporter = new ExtentSparkReporter(reportDir + "/" + reportName);
			reporter.config().setDocumentTitle("SAK3R Coaching - Test Report");
			reporter.config().setReportName("Automation Report");
			reporter.config().setTheme(Theme.DARK);
			reporter.config().setTimelineEnabled(true);

			extent = new ExtentReports();
			extent.attachReporter(reporter);
			extent.setSystemInfo("App", "SAK3R Coaching");
			extent.setSystemInfo("URL", ConfigReader.get("base.url"));
			extent.setSystemInfo("Browser", ConfigReader.get("browser.name"));
			extent.setSystemInfo("Environment", "Production (Vercel)");
			extent.setSystemInfo("Author", "QA Team");
		}
		return extent;
	}
}
