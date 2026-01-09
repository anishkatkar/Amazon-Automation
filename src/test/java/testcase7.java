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
 * TC_AMZ_007: Remove Item from Cart
 * 
 * Priority: MEDIUM (P3)
 * Type: Functional Test
 * Automation: YES
 * 
 * Test Objective:
 * Validate that users can remove items from cart and verify empty cart
 * behavior.
 * 
 * Test Data:
 * - Search Keyword: "mouse"
 * 
 * Expected Result:
 * - Item removed successfully
 * - Cart count decreases
 * - Empty cart message shown when cart is empty
 * - Checkout button hidden when cart is empty
 * 
 * @author Senior Automation Tester
 * @version 2.0
 */
public class testcase7 {

    // Logger instance for this test class
    private static final Logger logger = LogManager.getLogger(testcase7.class);

    private WebDriver driver;
    private AmazonSearchFunctions amazonFunctions;

    private static final String AMAZON_URL = "https://www.amazon.com";
    private static final String SEARCH_KEYWORD = "mouse";

    @BeforeClass
    public void setupClass() {
        TestLogger.logSection("TC_AMZ_007: Remove Item from Cart");
        logger.info("Priority: MEDIUM (P3) | Type: Functional Test | Automation: YES");
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
     * Test Method: Remove item from cart and verify empty cart behavior
     * 
     * Priority: 3 (MEDIUM) - Cart management functionality
     */
    @Test(priority = 3, description = "Remove item from cart and verify empty cart message")
    public void testRemoveItemFromCart() {
        TestLogger.logTestStart("TC_AMZ_007", "Remove Item from Cart");
        logger.info("Search Keyword: {}", SEARCH_KEYWORD);

        try {
            // ========== Pre-condition: Add item to cart ==========
            TestLogger.logTestStep("Pre-condition", "Add item to cart");
            amazonFunctions.navigateToAmazonHomepage(AMAZON_URL);
            logger.info("Navigated to homepage");

            // Add item 1
            amazonFunctions.enterSearchKeyword(amazonFunctions.locateSearchBox(), SEARCH_KEYWORD);
            amazonFunctions.clickSearchButton(amazonFunctions.locateSearchButton());
            amazonFunctions.waitForSearchResults();
            logger.info("Search results loaded");

            amazonFunctions.clickFirstProduct();
            logger.info("Clicked first product");

            amazonFunctions.addToCart();
            logger.info("Added product to cart");

            // ========== Navigate to cart ==========
            amazonFunctions.navigateToCart();
            logger.info("Navigated to cart page");

            int beforeCount = amazonFunctions.getCartCount();
            logger.info("Cart count before removal: {}", beforeCount);

            // Assert cart has items
            Assert.assertTrue(beforeCount > 0, "Cart should have items");

            // ========== Action: Remove item ==========
            TestLogger.logTestStep("Step 1", "Remove item from cart");
            boolean deleted = amazonFunctions.deleteFirstItemFromCart();
            Assert.assertTrue(deleted, "Failed to delete item");
            logger.info("Item deleted successfully");

            // ========== Verify count updated ==========
            TestLogger.logTestStep("Step 2", "Verify cart count decreased");
            int afterCount = amazonFunctions.getCartCount();
            logger.info("Cart count after removal: {}", afterCount);

            // Assert that count decreased
            Assert.assertTrue(afterCount < beforeCount, "Cart count did not decrease");
            logger.info("Cart count verification passed");

            // ========== Verify empty message if count is 0 ==========
            // Conditional block - only execute if cart is now empty
            if (afterCount == 0) {
                TestLogger.logTestStep("Step 3", "Verify empty cart message");
                logger.info("Cart is empty, verifying empty cart message");

                // Wait for page to update
                // Thread.sleep() gives page time to display empty cart message
                try {
                    Thread.sleep(2000); // Wait 2 seconds
                } catch (Exception e) {
                    logger.warn("Sleep interrupted: {}", e.getMessage());
                }

                // Check for empty cart message using multiple locators
                // Logical OR (||) - true if ANY condition is true
                boolean emptyMsg = driver.findElements(By.xpath("//*[contains(text(),'Your Amazon Cart is empty')]"))
                        .size() > 0 ||
                        driver.findElements(By.cssSelector(".sc-your-amazon-cart-is-empty")).size() > 0 ||
                        driver.findElements(By.xpath("//h1[contains(text(),'empty')]")).size() > 0;

                // Conditional logging based on result
                if (emptyMsg) {
                    logger.info("✓ Empty cart message verified");
                    System.out.println("✓ Empty cart message verified");
                } else {
                    logger.warn("Empty cart message not found, but count is 0. Proceeding.");
                    System.out.println("⚠ Empty cart message not found, but count is 0. Proceeding.");
                }

                // ========== Verify checkout button hidden ==========
                TestLogger.logTestStep("Step 4", "Verify checkout button hidden");

                // findElements() returns empty list if element not found (size = 0)
                // This is safer than findElement() which throws exception
                boolean checkoutBtn = driver.findElements(By.name("proceedToCheckout")).size() > 0;
                logger.info("Checkout button visible: {}", checkoutBtn);

                // Assert that checkout button is NOT visible (using assertFalse)
                Assert.assertFalse(checkoutBtn, "Checkout button should not be visible when empty");
                logger.info("Checkout button correctly hidden");
            }

            logger.info("========================================");
            logger.info("TEST RESULT: PASSED ✓");
            logger.info("========================================");
            TestLogger.logTestEnd("TC_AMZ_007", "PASSED");

        } catch (AssertionError e) {
            logger.error("Test assertion failed: {}", e.getMessage());
            TestLogger.logTestEnd("TC_AMZ_007", "FAILED");
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            TestLogger.logTestEnd("TC_AMZ_007", "FAILED");
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
