import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.*;
import utils.TestLogger;

import java.util.List;

/**
 * TC_AMZ_003: Verify Search with Price Filter
 * 
 * Priority: HIGH (P2)
 * Type: Functional Test
 * Automation: YES
 * 
 * Test Objective:
 * Validate that price filter functionality works correctly and filters
 * products within the specified price range.
 * 
 * Test Data:
 * - Search Keyword: "laptop"
 * - Price Range: $500 - $1000
 * 
 * Expected Result:
 * - Price filter applied successfully
 * - All displayed products are within the price range
 * 
 * @author Senior Automation Tester
 * @version 2.0
 */
public class testcase3 {

    // Logger instance for this test class
    private static final Logger logger = LogManager.getLogger(testcase3.class);

    private WebDriver driver;
    private AmazonSearchFunctions amazonFunctions;

    // Test Data
    private static final String AMAZON_URL = "https://www.amazon.com";
    private static final String SEARCH_KEYWORD = "laptop";
    private static final double MIN_PRICE = 500.0;
    private static final double MAX_PRICE = 1000.0;

    @BeforeClass
    public void setupClass() {
        TestLogger.logSection("TC_AMZ_003: Verify Search with Price Filter");
        logger.info("Priority: HIGH (P2) | Type: Functional Test | Automation: YES");
        logger.info("Price Range: ${} - ${}", MIN_PRICE, MAX_PRICE);
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
     * Test Method: Verify search results can be filtered by price
     * 
     * Priority: 2 (HIGH) - Important functional test
     */
    @Test(priority = 2, description = "Verify search results can be filtered by price")
    public void testPriceFilter() {
        TestLogger.logTestStart("TC_AMZ_003", "Verify Price Filter");
        logger.info("Search Keyword: {}", SEARCH_KEYWORD);

        try {
            // ========== Step 1: Navigate & Search "laptop" ==========
            TestLogger.logTestStep("Step 1", "Navigate to Amazon and search for '" + SEARCH_KEYWORD + "'");
            amazonFunctions.navigateToAmazonHomepage(AMAZON_URL);

            WebElement searchBox = amazonFunctions.locateSearchBox();
            amazonFunctions.clearSearchBox(searchBox);
            amazonFunctions.enterSearchKeyword(searchBox, SEARCH_KEYWORD);
            logger.info("Keyword entered: {}", SEARCH_KEYWORD);

            WebElement searchButton = amazonFunctions.locateSearchButton();
            amazonFunctions.clickSearchButton(searchButton);

            // Explicit wait for search results
            amazonFunctions.waitForSearchResults();
            logger.info("Initial search results loaded");

            // ========== Step 2: Apply Price Filter ($500 - $1000) ==========
            TestLogger.logTestStep("Step 2", "Apply price filter: $" + MIN_PRICE + " - $" + MAX_PRICE);
            boolean filterClicked = amazonFunctions.applyPriceFilter500to1000();
            Assert.assertTrue(filterClicked, "Failed to apply price filter");
            logger.info("Price filter applied successfully");

            // ========== Wait for results to reload ==========
            // Thread.sleep() is used here as a safety measure
            // After clicking filter, page needs time to reload with filtered results
            // This is an implicit wait - waits for a fixed duration
            // Note: Explicit waits (WebDriverWait) are preferred, but sometimes
            // a small Thread.sleep() is needed for AJAX-heavy pages like Amazon
            try {
                logger.debug("Waiting 3 seconds for filtered results to load...");
                Thread.sleep(3000); // 3000 milliseconds = 3 seconds
                logger.debug("Wait completed");
            } catch (InterruptedException e) {
                // InterruptedException occurs if thread is interrupted during sleep
                // We catch it but don't need to do anything
                logger.warn("Thread sleep interrupted: {}", e.getMessage());
            }

            // Additional explicit wait for search results
            amazonFunctions.waitForSearchResults();
            logger.info("Filtered results loaded");

            // ========== Step 3: Extract Prices ==========
            TestLogger.logTestStep("Step 3", "Extract all product prices");
            List<Double> prices = amazonFunctions.getAllProductPrices();
            logger.info("Extracted {} prices from products", prices.size());

            // Assert that we found at least some prices
            Assert.assertFalse(prices.isEmpty(), "No price elements found after filtering");
            logger.info("Price extraction successful");

            // ========== Step 4: Validate Price Range ==========
            TestLogger.logTestStep("Step 4", "Validate all prices are within range");
            boolean allInRange = amazonFunctions.verifyPricesInRange(prices, MIN_PRICE, MAX_PRICE);

            // Log individual prices for debugging
            logger.debug("Prices found: {}", prices);

            // Assert that all prices are within the expected range
            Assert.assertTrue(allInRange,
                    "Found products outside the expected price range $" + MIN_PRICE + "-" + MAX_PRICE);
            logger.info("All prices verified to be within range ${} - ${}", MIN_PRICE, MAX_PRICE);

            logger.info("========================================");
            logger.info("TEST RESULT: PASSED ✓");
            logger.info("========================================");
            TestLogger.logTestEnd("TC_AMZ_003", "PASSED");

        } catch (AssertionError e) {
            logger.error("Test assertion failed: {}", e.getMessage());
            TestLogger.logTestEnd("TC_AMZ_003", "FAILED");
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            TestLogger.logTestEnd("TC_AMZ_003", "FAILED");
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
        logger.info("ALL TESTS COMPLETED FOR TC_AMZ_003");
    }
}
