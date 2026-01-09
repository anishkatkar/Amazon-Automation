import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.*;
import utils.ScreenshotListener;
import utils.TestLogger;

/**
 * TC_AMZ_002: Verify Search with Invalid Keyword (Negative Test)
 * 
 * Priority: LOW (P4)
 * Type: Negative Test
 * Automation: YES
 * 
 * Test Objective:
 * Validate that searching with an invalid/gibberish keyword shows appropriate
 * "no results" message or suggestions. This is a negative test case.
 * 
 * Test Data:
 * - Invalid Keyword: "xyzabc123!@#$%"
 * 
 * Expected Result:
 * - "No results" message displayed OR suggestions shown
 * - Zero product cards displayed
 * 
 * @author Senior Automation Tester
 * @version 2.0
 */
public class testcase2 {

    // Logger instance for this test class
    private static final Logger logger = LogManager.getLogger(testcase2.class);

    private WebDriver driver;
    private AmazonSearchFunctions amazonFunctions;

    // Test Data
    private static final String AMAZON_URL = "https://www.amazon.com";
    private static final String INVALID_KEYWORD = "xyzabc123!@#$%";

    @BeforeClass
    public void setupClass() {
        TestLogger.logSection("TC_AMZ_002: Verify Search with Invalid Keyword");
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

        driver = new ChromeDriver(options);
        amazonFunctions = new AmazonSearchFunctions(driver);

        logger.info("--- Test Setup Completed ---");
    }

    /**
     * Test Method: Verify search behavior with invalid keywords
     * 
     * Priority: 4 (LOW) - Negative test case
     */
    @Test(priority = 4, description = "Verify search behavior with invalid keywords")
    public void testInvalidKeywordSearch() {
        TestLogger.logTestStart("TC_AMZ_002", "Verify Invalid Search");
        logger.info("Invalid Keyword: {}", INVALID_KEYWORD);

        try {
            // ========== Step 1: Navigate to Amazon ==========
            TestLogger.logTestStep("Step 1", "Navigate to Amazon homepage");
            amazonFunctions.navigateToAmazonHomepage(AMAZON_URL);
            logger.info("Navigation successful");

            // ========== Step 2 & 3: Enter invalid keyword ==========
            TestLogger.logTestStep("Step 2-3", "Locate search box and enter invalid keyword");
            WebElement searchBox = amazonFunctions.locateSearchBox();
            amazonFunctions.clearSearchBox(searchBox);
            amazonFunctions.enterSearchKeyword(searchBox, INVALID_KEYWORD);
            logger.info("Invalid keyword '{}' entered", INVALID_KEYWORD);

            // ========== Step 4: Click search ==========
            TestLogger.logTestStep("Step 4", "Click search button");
            WebElement searchButton = amazonFunctions.locateSearchButton();
            amazonFunctions.clickSearchButton(searchButton);

            // Wait for results page to load
            // Using explicit wait (WebDriverWait) inside waitForSearchResults()
            // This waits up to 10 seconds for search results container to appear
            amazonFunctions.waitForSearchResults();
            logger.info("Search results page loaded");

            // ========== Step 6 & 7: Check for "no results" OR suggestions ==========
            TestLogger.logTestStep("Step 5-6", "Verify 'No results' message or suggestions");
            boolean noResultsMsg = amazonFunctions.verifyNoResultsMessage();
            boolean hasSuggestions = amazonFunctions.verifySuggestionsDisplayed();

            logger.info("No Results Message Found: {}", noResultsMsg);
            logger.info("Suggestions Found: {}", hasSuggestions);

            // If neither condition is met, capture debug screenshot
            // This is a conditional block that helps with troubleshooting
            if (!noResultsMsg && !hasSuggestions) {
                logger.error("Neither 'No results' message nor suggestions found");
                logger.info("Capturing debug screenshot...");
                String screenshotPath = ScreenshotListener.captureScreenshot(driver, "testInvalidKeywordSearch_DEBUG");
                logger.info("Debug screenshot saved: {}", screenshotPath);
            }

            // Assert that at least one condition is true
            // Logical OR (||) - test passes if either condition is true
            Assert.assertTrue(noResultsMsg || hasSuggestions,
                    "Expected either 'No results' message or suggestions to be displayed");
            logger.info("Verification passed: No results or suggestions displayed");

            // ========== Step 8: Verify no product cards ==========
            TestLogger.logTestStep("Step 7", "Verify zero product cards displayed");
            int productCount = amazonFunctions.countProductCards();
            logger.info("Product count: {}", productCount);

            // Assert that product count is exactly 0
            Assert.assertEquals(productCount, 0,
                    "Expected 0 results for invalid keyword but found " + productCount);
            logger.info("Verification passed: Zero products displayed");

            logger.info("========================================");
            logger.info("TEST RESULT: PASSED ✓");
            logger.info("========================================");
            TestLogger.logTestEnd("TC_AMZ_002", "PASSED");

        } catch (AssertionError e) {
            logger.error("Test assertion failed: {}", e.getMessage());
            TestLogger.logTestEnd("TC_AMZ_002", "FAILED");
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            TestLogger.logTestEnd("TC_AMZ_002", "FAILED");
            throw e;
        }
    }

    @AfterMethod
    public void teardown() {
        logger.info("--- Test Teardown Started ---");
        if (driver != null) {
            // Uncomment to close browser after test
            // driver.quit();
            logger.info("Browser session maintained");
        }
        logger.info("--- Test Teardown Completed ---");
    }

    @AfterClass
    public void cleanupClass() {
        if (driver != null) {
            driver.quit();
            logger.info("Browser closed successfully");
        }
        logger.info("ALL TESTS COMPLETED FOR TC_AMZ_002");
    }
}
