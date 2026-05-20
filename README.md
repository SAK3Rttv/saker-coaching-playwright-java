# SAK3R Coaching - Playwright Java Test Automation

![Java](https://img.shields.io/badge/Java-17-orange?logo=java)
![Playwright](https://img.shields.io/badge/Playwright-Java-brightgreen?logo=playwright)
![TestNG](https://img.shields.io/badge/TestNG-7.x-red)
![Maven](https://img.shields.io/badge/Maven-3.x-blue?logo=apachemaven)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

End-to-end test automation suite for [SAK3R Coaching](https://saker-coaching.vercel.app/) built with **Playwright Java**, **TestNG**, and **ExtentReports**. Covers login, navigation, and user flows with data-driven testing via Excel.

---

## Tech Stack

| Tool | Purpose |
|------|---------|
| Playwright Java | Browser automation |
| TestNG | Test framework & data providers |
| ExtentReports | HTML test reporting |
| Apache POI | Excel-driven data provider |
| Maven | Build & dependency management |

---

## Project Structure

```
src/test/java/
├── base/
│   └── BaseTest.java          # Playwright setup/teardown, browser context
├── config/
│   └── ConfigReader.java      # Reads Config.properties
├── listeners/
│   └── TestListener.java      # ExtentReports wiring via ITestListener
├── pages/
│   ├── HomePage.java          # Landing page & navbar interactions
│   ├── LoginPage.java         # Login modal page object
│   └── NavbarComponent.java   # Shared navbar component
├── tests/
│   ├── functional/
│   │   └── LoginTest.java     # Data-driven login tests (29 cases)
│   ├── smoke/
│   ├── security/
│   └── e2e/
└── utils/
    ├── WaitUtil.java          # Smart wait helpers - no Thread.sleep()
    ├── ExcelUtil.java         # Apache POI Excel data provider
    ├── ExtentManager.java     # Singleton ExtentReports instance
    └── ScreenshotUtil.java    # Auto screenshot on failure

src/test/resources/
├── Config.properties          # Browser, URL, timeout, credential config
└── LoginTestData.xlsx         # 29 login test cases (Positive/Negative/Edge/Security)
```

---

## Test Coverage

### Login Feature - 29 Data-Driven Test Cases

| Category | Count | What's Tested |
|----------|-------|---------------|
| ✅ Positive | 6 | Valid credentials, remember me, role-based login (user/coach/owner) |
| ❌ Negative | 10 | Wrong password, empty fields, bad format, unverified account |
| ⚠️ Edge | 8 | 255-char email, unicode password, special characters, boundary values |
| 🔒 Security | 5 | XSS, SQL injection, NoSQL injection, null byte injection |

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.6+
- Git

### Installation

```bash
# Clone the repository
git clone https://github.com/YOUR_USERNAME/saker-coaching-playwright-java.git
cd saker-coaching-playwright-java

# Install dependencies
mvn install -DskipTests

# Install Playwright browsers
mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"
```

### Configuration

Edit `src/test/resources/Config.properties`:

```properties
# Base URL
base.url = https://saker-coaching.vercel.app/

# Browser settings
browser.name     = chromium       # chromium | firefox | webkit
browser.headless = true
browser.slow.mo  = 0

# Timeouts (ms)
timeout.default    = 15000
timeout.navigation = 30000
timeout.element    = 15000

# Test credentials - replace with real accounts
user.email    = your@email.com
user.password = yourpassword

# --- Reporting ---------------------------------------
report.output.dir = test-output
report.html.name = SAK3R-ExtentReport.html
screenshots.dir = test-output/screenshots
```

> ⚠️ Never commit real credentials. Add `Config.properties` to `.gitignore` and use environment variables in CI.

### Running Tests

```bash
# Run all tests
mvn test

# Run specific suite
mvn test -Dsurefire.suiteXmlFiles=testng.xml

# Run in headed mode (watch browser)
mvn test -Dbrowser.headless=false
```

---

## Reports

After each run, reports are generated in `test-output/`:

```
test-output/
├── SAK3R-ExtentReport.html     # Full HTML report with logs & screenshots
└── screenshots/                # Auto-captured on test failure
```

Open `SAK3R-ExtentReport.html` in any browser to view results.

---

## Key Design Decisions

**No `Thread.sleep()`** - all waits use Playwright's `waitForCondition` and `waitFor` with explicit conditions, making tests fast on fast environments and reliable on slow ones.

**Isolated browser context per test** - each test gets a fresh `BrowserContext` with cleared cookies and localStorage, preventing session bleed between tests.

**Security payload masking** - XSS and injection payloads are masked in reports (`*** [security payload - masked] ***`) to prevent report-level XSS execution.

**Config-driven** - browser, timeouts, credentials, and URLs are all externalized in `Config.properties`, no hardcoded values in test code.

---

## CI/CD

```yaml
# .github/workflows/tests.yml
name: Playwright Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Install Playwright browsers
        run: mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps"
      - name: Run tests
        run: mvn test
      - name: Upload report
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: extent-report
          path: test-output/
```

---

## Author

Built as a professional QA automation portfolio project covering real-world patterns: Page Object Model, data-driven testing, smart waits, security testing, and CI integration.

---

## License

MIT
