# SauceDemo Selenium + Java Automation Framework

A Page Object Model UI automation framework built against
[saucedemo.com](https://www.saucedemo.com/), covering login (4 variants),
product sort, add-to-cart, end-to-end checkout, and remove-from-cart.

## Stack

- Java 17, Maven
- Selenium 4.34 (WebDriver, Selenium Manager — no checked-in driver binaries)
- TestNG 7.11 (DataProvider, parallel execution, retry, listeners)
- ExtentReports 5.1.2 (HTML report, screenshot-on-failure)
- Log4j2 (console + rolling file log)
- Apache POI (Excel-driven login test data)
- Jenkins declarative pipeline

## Project layout

```
src/main/java/
  base/BaseTest.java         # ThreadLocal<WebDriver> setup/teardown — thread-safe for parallel runs
  pages/                     # Page Object classes — private By locator fields + driver.findElement,
                              #   matching the reference framework's pattern (not PageFactory @FindBy)
  utils/ConfigReader.java    # classpath-based config loader (no hardcoded filesystem paths)
  utils/WaitUtils.java       # centralized explicit waits, works off By locators
  utils/ExtentManager.java   # ExtentReports singleton, thread-safe per-test nodes
  utils/ProductCatalog.java  # reads "Products" sheet - shared reference data for Cart/Checkout tests
  listeners/TestListener.java     # wires TestNG events -> ExtentReports; captures screenshot-on-failure
  listeners/RetryAnalyzer.java    # retries a failed test N times (config: retry.count)
  listeners/RetryTransformer.java # auto-applies RetryAnalyzer to every @Test

src/test/java/
  tests/                     # LoginTest, SortTest, CartTest, CheckoutTest
  dataproviders/ExcelDataProvider.java   # single class, one helper (readSheet), two
                                          #   @DataProvider methods (loginData, checkoutData)
                                          #   — matches the reference framework's
                                          #   ExcelDataProvider pattern

src/test/resources/
  config.properties          # browser, base URL, timeouts, retry count, checkout tax rate
  log4j2.xml
  testdata/TestData.xlsx     # single workbook, 3 sheets - see below

testng.xml                   # main suite — parallel="classes", thread-count=3
testng-crossbrowser.xml      # optional — parallel="tests" across chrome/firefox
Jenkinsfile
```

## Test cases covered

| ID | Scenario | Class | Data source |
|---|---|---|---|
| 1a-1d | Login: valid, invalid password, locked-out, empty fields | `LoginTest` | Excel - `TestData.xlsx` → `Login` sheet |
| 2 | Product sort (Name A-Z/Z-A, Price low-high/high-low) | `SortTest` | none - validates the site's live-rendered order against itself; there's no external "expected" data to source |
| 3 | Add to cart + badge count | `CartTest.addToCartAndVerifyBadgeTest` | Excel - `TestData.xlsx` → `Products` sheet (via `ProductCatalog`) |
| 5 | Remove item from cart | `CartTest.removeItemFromCartTest` | Excel - `TestData.xlsx` → `Products` sheet (via `ProductCatalog`) |
| 4 | End-to-end checkout with totals + confirmation | `CheckoutTest` | Excel - `TestData.xlsx` → `Checkout` sheet + `Products` sheet |

Matches the manual test case sheet delivered earlier (`Saucedemo_Test_Cases.xlsx` / `.docx`).

### TestData.xlsx sheet structure

One workbook (`src/test/resources/testdata/TestData.xlsx`), three sheets:

| Sheet | Columns | Used by | Role |
|---|---|---|---|
| `Login` | TestCaseId, Username, Password, ExpectedResult, ExpectedMessage | `ExcelDataProvider.getLoginData()` (Map-based — see Design notes) | Per-test-case rows — each row is a full login scenario |
| `Products` | Slug, Name, Price | `ProductCatalog` | Reference/catalog data, not test cases — `CartTest` and `CheckoutTest` look up names and prices here instead of hardcoding them, so a price or name change on the site is a one-line edit in the sheet |
| `Checkout` | TestCaseId, FirstName, LastName, ZipCode, CartItemSlugs | `ExcelDataProvider.getCheckoutData()` (Map-based — see Design notes) | Per-test-case rows — `CartItemSlugs` is comma-separated slugs resolved against `Products` to add items and compute the expected item total, tax (`checkout.tax.rate` in config.properties, default 8%), and grand total dynamically |

**Note on naming:** there's no `Registration` sheet — saucedemo.com is login-only with no
sign-up flow, so a registration sheet would have nothing real to drive. `Checkout` sheet
takes that slot instead, since it's genuine form data actually exercised by a test.

## Design notes

**Locator strategy.** Page objects use private `By` locator fields and
`driver.findElement(locator)` / `WaitUtils` (which wraps `WebDriverWait` +
`ExpectedConditions` around a `By`), not PageFactory `@FindBy` proxies — this
matches the pattern in the reference framework (`Catalog.java`'s `private By
catalogLink = By.linkText(...)` + `utility.click(locator)`).

**Screenshot-on-failure — the bug and the fix.** The first version of this
framework captured the screenshot inside `BaseTest`'s `@AfterMethod` and
attached it to the `ITestResult`, expecting `TestListener.onTestFailure()` to
read it back out. That never worked: TestNG invokes `onTestFailure()`
immediately after the failed `@Test` method returns, and only *afterwards*
runs `@AfterMethod`/`@AfterClass` — so the attribute the listener was reading
hadn't been set yet, and the ExtentReports entry always ended up with an empty
screenshot. The fix moves capture directly into
`TestListener.onTestFailure()`, using `BaseTest.getDriver()` while the browser
session is still open (before `tearDown()` calls `driver.quit()`).

**One DataProvider read method, not two.** `ExcelDataProvider` has a single
`readSheet(fileName, sheetName)` used for every sheet — each row becomes a
`Map<String,String>` keyed by header text, wrapped as a single-argument row
(`loginTest(Map<String,String> row)`, `endToEndCheckoutTest(Map<String,String>
row)`). Taking the workbook file name as a parameter (not a hardcoded
constant) means it's already reusable for a second workbook later — e.g.
`readSheet("RegressionData.xlsx", "Products")` — without editing the method.
An earlier version had two read methods — positional `Object[]` rows for the
small, stable `Login` sheet, and map rows for `Checkout` (expected to grow
well past what's comfortable as a positional parameter list, e.g. 20+
columns). Standardizing both sheets on the map shape removes that
duplication: a sheet growing from 5 columns to 25 never needs a different
read method, just `row.get("NewColumn")` in the test. The trade-off is losing
compile-time type safety — a typo'd key like `row.get("Zip")` vs
`row.get("ZipCode")` is only caught at runtime, not by the compiler.

## Running locally

```bash
mvn clean test                                   # default: testng.xml, chrome, headed
mvn clean test -Dbrowser=firefox                 # override browser
mvn clean test -Dheadless=true                   # run headless
mvn clean test -Dsuite.file=testng-crossbrowser.xml   # chrome + firefox in parallel
```

Requires Chrome/Firefox/Edge installed locally — Selenium Manager resolves the
matching driver automatically, nothing to download or configure.

## Reports & logs

- ExtentReports HTML: `test-output/extent-reports/SauceDemo_Report_<timestamp>.html`
- Log4j2 log file: `test-output/logs/automation.log`
- Standard TestNG/Surefire XML: `target/surefire-reports/`

## Jenkins

`Jenkinsfile` defines a parameterized pipeline (browser, suite file, headless
toggle) that runs the suite via Maven and publishes the ExtentReports HTML
using the HTML Publisher plugin. Requires a `Maven3` tool and `JDK17` tool
configured in Jenkins global tool config, and the HTML Publisher plugin
installed for the `publishHTML` step.

## Known limitations / next steps

- No API-layer setup/teardown (cart state is built through the UI each test —
  fine at this scale, would move to seeded state via API for a larger suite).
- Cross-browser suite currently covers `LoginTest` only; extend
  `testng-crossbrowser.xml`'s `<classes>` block to add more.
- Not yet verified with an actual `mvn test` run in this environment (the
  sandbox used to build this framework can't reach Maven Central to resolve
  dependencies) — recommend running `mvn clean test` locally as the first
  step after downloading, and treating that as the real acceptance check.
