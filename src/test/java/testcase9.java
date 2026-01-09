import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.*;
import utils.TestLogger;

/**
 * TC_AMZ_009: Empty Search Validation (Negative Test)
 * 
 * Priority: LOW (P4)
 * Type: Negative Test
 * Automation: YES
 * 
 * Test Objective:
 * Validate that clicking search with empty search box doesn't cause errors
 * and handles gracefully. This is a negative test case.
 * 
 * Expected Result:
 * - No application crash or error page
 * - Stays on Amazon domain
 * - No navigation to invalid URL
 * 
 * @author Senior Automation Tester
 * @version 2.0
 */
public class testcase9 {

    // Logger instance for this test class
    private static final Logger logger = LogManager.getLogger(testcase9.class);

    private WebDriver driver;
    private AmazonSearchFunctions amazonFunctions;

    private static final String AMAZON_URL = "https://www.amazon.com";

    @BeforeClass
    public void setupClass() {
        TestLogger.logSection("TC_AMZ_009: Empty Search Validation");
        logger.info("Priority: LOW (P4) | Type: Negative Test | Automation: YES");
        WebDriverManager.chromedriver().setup();
        logger.info("WebDriverManager setup completed");
    }

    @BeforeMethod
    public void setup() {
        logger.info("--- Test Setup Started ---");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");

        driver = new ChromeDriver(options);
        amazonFunctions = new AmazonSearchFunctions(driver);

        logger.info("--- Test Setup Completed ---");
    }

    /**
     * Test Method: Verify empty search behavior
     * 
     * Priority: 4 (LOW) - Negative test case
     */
    @Test(priority = 4, description = "Verify empty search doesn't cause errors")
    public void testEmptySearch() {
        TestLogger.logTestStart("TC_AMZ_009", "Empty Search Validation");

        try {
            // ========== Step 1: Navigate to homepage ==========
            TestLogger.logTestStep("Step 1", "Navigate to Amazon homepage");
            amazonFunctions.navigateToAmazonHomepage(AMAZON_URL);
            logger.info("Navigated to homepage");

            // ========== Step 2-3: Ensure search box is empty ==========
            TestLogger.logTestStep("Step 2", "Clear search box to ensure it's empty");

            // clear() method removes any existing text from input field
            amazonFunctions.locateSearchBox().clear();
            logger.info("Search box cleared");

            // ========== Step 4: Click search button directly ==========
            TestLogger.logTestStep("Step 3", "Click search button with empty search box");
            amazonFunctions.clickSearchButton(amazonFunctions.locateSearchButton());
            logger.info("Search button clicked with empty input");

            // Wait for page to respond
            // Thread.sleep() is used here because we're testing error handling
            // We want to give the page time to show any error messages
            try {
                Thread.sleep(2000); // Wait 2 seconds
            } catch (InterruptedException e) {
                logger.warn("Sleep interrupted: {}", e.getMessage());
            }

            // ========== Step 5-7: Verify behavior ==========
            TestLogger.logTestStep("Step 4", "Verify no errors and proper handling");

            // Expectation: Stays on homepage or shows products, NO error crash
            String currentUrl = driver.getCurrentUrl();
            logger.info("URL after empty search: {}", currentUrl);
            System.out.println("URL after empty search: " + currentUrl);

            // ========== Verification 1: Domain is still amazon.com ==========
            // contains() checks if URL string contains "amazon.com"
            Assert.assertTrue(currentUrl.contains("amazon.com"),
                    "Navigated away from Amazon domain");
            logger.info("Domain verification passed");

            // ========== Verification 2: Not an error page ==========
            // getTitle() retrieves the page title
            // Logical OR (||) - fails if either condition is true
            // We're checking that title does NOT contain error indicators
            Assert.assertFalse(
                    driver.getTitle().contains("Error") || driver.getTitle().contains("Page Not Found"),
                    "Landed on an error page");
            logger.info("Error page check passed");

            logger.info("========================================");
            logger.info("TEST RESULT: PASSED ✓");
            logger.info("========================================");
            TestLogger.logTestEnd("TC_AMZ_009", "PASSED");

        } catch (AssertionError e) {
            logger.error("Test assertion failed: {}", e.getMessage());
            TestLogger.logTestEnd("TC_AMZ_009", "FAILED");
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            TestLogger.logTestEnd("TC_AMZ_009", "FAILED");
            throw e;
        }
    }

    @AfterMethod
    public void teardown() {
        logger.info("--- Test Teardown Started ---");
        if (driver != null) {
            driver.quit();
            logger.info("Browser closed");
        }
        logger.info("--- Test Teardown Completed ---");
    }
}
