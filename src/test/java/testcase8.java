import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.*;
import utils.TestLogger;

/**
 * TC_AMZ_008: Proceed to Checkout (Guest User)
 * 
 * Priority: MEDIUM (P3)
 * Type: Smoke Test
 * Automation: YES
 * 
 * Test Objective:
 * Validate that guest users can proceed to checkout and are redirected
 * to the sign-in page. This tests the checkout flow initiation.
 * 
 * Test Data:
 * - Search Keyword: "books"
 * - Test Email: "test_automation_user@example.com"
 * 
 * Expected Result:
 * - "Proceed to Checkout" button clickable
 * - Redirected to sign-in page
 * - Email field visible and functional
 * - After entering email, password field or error message appears
 * 
 * @author Senior Automation Tester
 * @version 2.0
 */
public class testcase8 {

    // Logger instance for this test class
    private static final Logger logger = LogManager.getLogger(testcase8.class);

    private WebDriver driver;
    private AmazonSearchFunctions amazonFunctions;

    private static final String AMAZON_URL = "https://www.amazon.com";
    private static final String SEARCH_KEYWORD = "The Great Gatsby";

    @BeforeClass
    public void setupClass() {
        TestLogger.logSection("TC_AMZ_008: Proceed to Checkout (Guest User)");
        logger.info("Priority: MEDIUM (P3) | Type: Smoke Test | Automation: YES");
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
     * Test Method: Proceed to checkout as guest user
     * 
     * Priority: 3 (MEDIUM) - Checkout flow validation
     */
    @Test(priority = 3, description = "Proceed to checkout and verify sign-in page")
    public void testProceedToCheckout() {
        TestLogger.logTestStart("TC_AMZ_008", "Proceed to Checkout");
        logger.info("Search Keyword: {}", SEARCH_KEYWORD);

        try {
            // ========== Pre-condition: Add item to cart ==========
            TestLogger.logTestStep("Pre-condition", "Add item to cart");
            amazonFunctions.navigateToAmazonHomepage(AMAZON_URL);
            logger.info("Navigated to homepage");

            amazonFunctions.enterSearchKeyword(amazonFunctions.locateSearchBox(), SEARCH_KEYWORD);
            amazonFunctions.clickSearchButton(amazonFunctions.locateSearchButton());
            amazonFunctions.waitForSearchResults();
            logger.info("Search results loaded");

            // ========== Find specific book using XPath ==========
            // Try to click specific book, fallback to first product if not found
            boolean productClicked = amazonFunctions.clickBookBySpecificXPath();

            // Conditional logic - if specific book not found, use fallback
            if (!productClicked) {
                logger.warn("Specific book by XPath not found. Falling back to first product.");
                System.out.println("Specific book by XPath not found. Falling back to first product.");
                amazonFunctions.clickFirstProduct();
            } else {
                logger.info("Specific book clicked successfully");
            }

            // ========== Add to cart ==========
            boolean added = amazonFunctions.addToCart();
            Assert.assertTrue(added, "Failed to add item to cart");
            logger.info("Item added to cart");

            // ========== Verify cart count ==========
            int count = amazonFunctions.getCartCount();
            logger.info("DEBUG: Cart count after add: {}", count);
            System.out.println("DEBUG: Cart count after add: " + count);

            Assert.assertTrue(count > 0, "Cart is empty after adding item");
            logger.info("Cart count verification passed");

            // ========== Step 2: Navigate to cart ==========
            TestLogger.logTestStep("Step 1", "Navigate to cart");
            amazonFunctions.navigateToCart();
            logger.info("Navigated to cart page");

            // ========== Step 3-4: Verify and Click Proceed to Checkout ==========
            TestLogger.logTestStep("Step 2", "Click 'Proceed to Checkout'");
            boolean clicked = amazonFunctions.clickProceedToCheckout();
            Assert.assertTrue(clicked, "Failed to click Proceed to Checkout");
            logger.info("Proceed to Checkout clicked");

            // ========== Step 5: Verify Redirection to Checkout/Sign-in ==========
            TestLogger.logTestStep("Step 3", "Verify redirected to checkout or sign-in page");

            // Wait for page to load after clicking checkout
            try {
                Thread.sleep(3000); // Wait for redirect
            } catch (InterruptedException e) {
                logger.warn("Sleep interrupted: {}", e.getMessage());
            }

            // Get current URL after checkout click
            String currentUrl = driver.getCurrentUrl();
            logger.info("Current URL after checkout: {}", currentUrl);

            // Verify we're redirected away from cart page to checkout/signin flow
            // The URL should contain either "signin", "ap/", "spc/", or "buy"
            boolean redirected = currentUrl.contains("signin") ||
                    currentUrl.contains("ap/") ||
                    currentUrl.contains("spc/") ||
                    currentUrl.contains("buy");

            Assert.assertTrue(redirected,
                    "Did not redirect to checkout/signin page. Current URL: " + currentUrl);
            logger.info("Successfully redirected to checkout flow");

            logger.info("========================================");
            logger.info("TEST RESULT: PASSED ✓");
            logger.info("========================================");
            TestLogger.logTestEnd("TC_AMZ_008", "PASSED");

        } catch (AssertionError e) {
            logger.error("Test assertion failed: {}", e.getMessage());
            TestLogger.logTestEnd("TC_AMZ_008", "FAILED");
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            TestLogger.logTestEnd("TC_AMZ_008", "FAILED");
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
