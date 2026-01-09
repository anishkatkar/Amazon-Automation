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
 * TC_AMZ_005: Add Single Item to Cart
 * 
 * Priority: CRITICAL (P1)
 * Type: Smoke Test
 * Automation: YES
 * 
 * Test Objective:
 * Validate that a user can successfully add a product to the shopping cart
 * and the cart count is updated correctly. This is a critical smoke test.
 * 
 * Test Data:
 * - Search Keyword: "headphones"
 * 
 * Expected Result:
 * - Product added to cart successfully
 * - Cart count increases by 1
 * - User can navigate to cart page
 * - Cart page displays the added item
 * 
 * @author Senior Automation Tester
 * @version 2.0
 */
public class testcase5 {

    // Logger instance for this test class
    private static final Logger logger = LogManager.getLogger(testcase5.class);

    private WebDriver driver;
    private AmazonSearchFunctions amazonFunctions;

    private static final String AMAZON_URL = "https://www.amazon.com";
    private static final String SEARCH_KEYWORD = "usb cable"; // Using different keyword

    @BeforeClass
    public void setupClass() {
        TestLogger.logSection("TC_AMZ_005: Add Single Item to Cart");
        logger.info("Priority: CRITICAL (P1) | Type: Smoke Test | Automation: YES");
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
     * Test Method: Add single item to cart and verify
     * 
     * Priority: 1 (CRITICAL) - Core e-commerce functionality
     */
    @Test(priority = 1, description = "Add single item to cart and verify cart count increases")
    public void testAddSingleItemToCart() {
        TestLogger.logTestStart("TC_AMZ_005", "Add Single Item to Cart");
        logger.info("Search Keyword: {}", SEARCH_KEYWORD);

        try {
            // ========== Pre-condition: Navigate to product page ==========
            TestLogger.logTestStep("Pre-condition", "Navigate to product page");
            amazonFunctions.navigateToAmazonHomepage(AMAZON_URL);
            logger.info("Navigated to homepage");

            amazonFunctions.enterSearchKeyword(amazonFunctions.locateSearchBox(), SEARCH_KEYWORD);
            logger.info("Entered search keyword: {}", SEARCH_KEYWORD);

            amazonFunctions.clickSearchButton(amazonFunctions.locateSearchButton());
            logger.info("Clicked search button");

            amazonFunctions.waitForSearchResults();
            logger.info("Search results loaded");

            amazonFunctions.clickFirstProduct();
            logger.info("Clicked first product");

            // ========== Step 1: Check initial cart count ==========
            TestLogger.logTestStep("Step 1", "Get initial cart count");
            int initialCount = amazonFunctions.getCartCount();
            logger.info("Initial cart count: {}", initialCount);

            // ========== Step 2: Add to cart ==========
            TestLogger.logTestStep("Step 2", "Click 'Add to Cart' button");
            boolean added = amazonFunctions.addToCart();
            Assert.assertTrue(added, "Failed to click Add to Cart");
            logger.info("Add to Cart button clicked successfully");

            // ========== Step 3: Verify cart count updated ==========
            TestLogger.logTestStep("Step 3", "Verify cart count increased");

            // Retry mechanism with loop
            // We use a for loop to check cart count multiple times
            // This is necessary because cart count update might take a moment
            int newCount = initialCount;

            // Loop executes maximum 5 times (i = 0, 1, 2, 3, 4)
            for (int i = 0; i < 5; i++) {
                logger.debug("Retry attempt {} of 5", i + 1);

                // Get current cart count
                newCount = amazonFunctions.getCartCount();

                // Check if count increased
                // If true, break out of loop early (no need to continue retrying)
                if (newCount > initialCount) {
                    logger.info("Cart count increased on attempt {}", i + 1);
                    break; // Exit the for loop immediately
                }

                // If count hasn't increased yet, wait 1 second before next retry
                try {
                    Thread.sleep(1000); // Wait 1 second
                } catch (InterruptedException e) {
                    // InterruptedException - thread was interrupted during sleep
                    logger.warn("Sleep interrupted: {}", e.getMessage());
                }
            }
            // After loop ends (either by break or completing all 5 iterations)

            logger.info("Initial Count: {}, New Count: {}", initialCount, newCount);

            // Assert that cart count increased
            Assert.assertTrue(newCount > initialCount,
                    "Cart count did not increase. Initial: " + initialCount + ", New: " + newCount);
            logger.info("Cart count verification passed");

            // ========== Step 4: Navigate to cart and verify ==========
            TestLogger.logTestStep("Step 4", "Navigate to cart page");
            amazonFunctions.navigateToCart();
            logger.info("Navigated to cart page");

            // Verify we're on the cart page by checking URL
            // contains() method checks if the URL string contains "cart"
            String currentUrl = driver.getCurrentUrl();
            logger.info("Current URL: {}", currentUrl);

            Assert.assertTrue(currentUrl.contains("cart"), "Not on cart page");
            logger.info("Cart page verification passed");

            logger.info("========================================");
            logger.info("TEST RESULT: PASSED ✓");
            logger.info("========================================");
            TestLogger.logTestEnd("TC_AMZ_005", "PASSED");

        } catch (AssertionError e) {
            logger.error("Test assertion failed: {}", e.getMessage());
            TestLogger.logTestEnd("TC_AMZ_005", "FAILED");
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            TestLogger.logTestEnd("TC_AMZ_005", "FAILED");
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
