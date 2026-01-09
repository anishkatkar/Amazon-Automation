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
 * TC_AMZ_010: Cart Persistence After Page Refresh
 * 
 * Priority: MEDIUM (P3)
 * Type: Functional Test
 * Automation: YES
 * 
 * Test Objective:
 * Validate that cart items persist after page refresh. This tests
 * session management and cart data persistence.
 * 
 * Test Data:
 * - Search Keyword: "monitor"
 * 
 * Expected Result:
 * - Cart items remain after page refresh
 * - Cart count stays the same
 * - Items visible on cart page after refresh
 * 
 * @author Senior Automation Tester
 * @version 2.0
 */
public class testcase10 {

    // Logger instance for this test class
    private static final Logger logger = LogManager.getLogger(testcase10.class);

    private WebDriver driver;
    private AmazonSearchFunctions amazonFunctions;

    private static final String AMAZON_URL = "https://www.amazon.com";
    private static final String SEARCH_KEYWORD = "monitor";

    @BeforeClass
    public void setupClass() {
        TestLogger.logSection("TC_AMZ_010: Cart Persistence After Page Refresh");
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
     * Test Method: Verify cart persistence after page refresh
     * 
     * Priority: 3 (MEDIUM) - Session management test
     */
    @Test(priority = 3, description = "Verify cart items persist after page refresh")
    public void testCartPersistence() {
        TestLogger.logTestStart("TC_AMZ_010", "Cart Persistence After Refresh");
        logger.info("Search Keyword: {}", SEARCH_KEYWORD);

        try {
            // ========== Step 1: Add items to cart ==========
            TestLogger.logTestStep("Step 1", "Add item to cart");
            amazonFunctions.navigateToAmazonHomepage(AMAZON_URL);
            logger.info("Navigated to homepage");

            amazonFunctions.enterSearchKeyword(amazonFunctions.locateSearchBox(), SEARCH_KEYWORD);
            amazonFunctions.clickSearchButton(amazonFunctions.locateSearchButton());
            amazonFunctions.waitForSearchResults();
            logger.info("Search results loaded");

            amazonFunctions.clickFirstProduct();
            logger.info("Clicked first product");

            amazonFunctions.addToCart();
            logger.info("Added product to cart");

            // ========== Step 2: Note initial count ==========
            TestLogger.logTestStep("Step 2", "Get cart count before refresh");
            int countBefore = amazonFunctions.getCartCount();

            // Assert that item was added successfully
            Assert.assertTrue(countBefore > 0, "Cart failed to add item");
            logger.info("Cart count before refresh: {}", countBefore);
            System.out.println("Cart count before refresh: " + countBefore);

            // ========== Step 3-4: Refresh page ==========
            TestLogger.logTestStep("Step 3", "Refresh the page");

            // refreshPage() calls driver.navigate().refresh()
            // This reloads the current page, simulating F5 key press
            amazonFunctions.refreshPage();
            logger.info("Page refreshed");

            // ========== Step 5: Check count again ==========
            TestLogger.logTestStep("Step 4", "Get cart count after refresh");
            int countAfter = amazonFunctions.getCartCount();
            logger.info("Cart count after refresh: {}", countAfter);
            System.out.println("Cart count after refresh: " + countAfter);

            // ========== Verify counts match ==========
            // assertEquals() checks if two values are exactly equal
            // This verifies that cart count didn't change after refresh
            Assert.assertEquals(countAfter, countBefore, "Cart items lost after refresh!");
            logger.info("Cart count verification passed - items persisted");

            // ========== Step 6-7: Navigate to cart and verify ==========
            TestLogger.logTestStep("Step 5", "Navigate to cart page and verify");
            amazonFunctions.navigateToCart();
            logger.info("Navigated to cart page");

            // Get cart count from cart page
            int cartPageCount = amazonFunctions.getCartCount();
            logger.info("Cart page count: {}", cartPageCount);

            // Verify cart page count matches original count
            Assert.assertEquals(cartPageCount, countBefore, "Cart page count mismatch");
            logger.info("Cart page count verification passed");

            logger.info("========================================");
            logger.info("TEST RESULT: PASSED ✓");
            logger.info("========================================");
            TestLogger.logTestEnd("TC_AMZ_010", "PASSED");

        } catch (AssertionError e) {
            logger.error("Test assertion failed: {}", e.getMessage());
            TestLogger.logTestEnd("TC_AMZ_010", "FAILED");
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            TestLogger.logTestEnd("TC_AMZ_010", "FAILED");
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
