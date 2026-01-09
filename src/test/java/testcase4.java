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
 * TC_AMZ_004: Verify Product Details Display
 * 
 * Priority: HIGH (P2)
 * Type: Smoke Test
 * Automation: YES
 * 
 * Test Objective:
 * Validate that clicking on a product from search results navigates to
 * the product details page and all essential elements are displayed.
 * 
 * Test Data:
 * - Search Keyword: "laptop"
 * 
 * Expected Result:
 * - Product details page loads successfully
 * - Product image, price, and "Add to Cart" button are visible
 * - Product title matches the clicked product
 * 
 * @author Senior Automation Tester
 * @version 2.0
 */
public class testcase4 {

    // Logger instance for this test class
    private static final Logger logger = LogManager.getLogger(testcase4.class);

    private WebDriver driver;
    private AmazonSearchFunctions amazonFunctions;

    private static final String AMAZON_URL = "https://www.amazon.com";
    private static final String SEARCH_KEYWORD = "laptop";

    @BeforeClass
    public void setupClass() {
        TestLogger.logSection("TC_AMZ_004: Verify Product Details Display");
        logger.info("Priority: HIGH (P2) | Type: Smoke Test | Automation: YES");
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
     * Test Method: Verify product details page displays correctly
     * 
     * Priority: 2 (HIGH) - Critical smoke test for product page
     */
    @Test(priority = 2, description = "Verify product details page displays all essential elements")
    public void testProductDetailsDisplay() {
        TestLogger.logTestStart("TC_AMZ_004", "Verify Product Details Display");
        logger.info("Search Keyword: {}", SEARCH_KEYWORD);

        try {
            // ========== Pre-condition: Search for product ==========
            TestLogger.logTestStep("Pre-condition", "Search for product");
            amazonFunctions.navigateToAmazonHomepage(AMAZON_URL);
            logger.info("Navigated to Amazon homepage");

            // Enter search keyword
            amazonFunctions.enterSearchKeyword(amazonFunctions.locateSearchBox(), SEARCH_KEYWORD);
            logger.info("Entered search keyword: {}", SEARCH_KEYWORD);

            // Click search button
            amazonFunctions.clickSearchButton(amazonFunctions.locateSearchButton());
            logger.info("Clicked search button");

            // Wait for search results to load
            amazonFunctions.waitForSearchResults();
            logger.info("Search results loaded");

            // ========== Step 1 & 2: Get title and click first product ==========
            TestLogger.logTestStep("Step 1-2", "Get product title and click first product");

            // Store the product title before clicking
            // This will be used later to verify we're on the correct product page
            String expectedTitle = amazonFunctions.getFirstProductTitle();
            logger.info("First product title: {}", expectedTitle);

            // Assert that title is not empty
            Assert.assertFalse(expectedTitle.isEmpty(), "Product title should not be empty");
            logger.info("Product title validation passed");

            // Click on the first product
            boolean clicked = amazonFunctions.clickFirstProduct();
            Assert.assertTrue(clicked, "Failed to click first product");
            logger.info("Successfully clicked first product");

            // ========== Step 3: Verify product details page ==========
            TestLogger.logTestStep("Step 3", "Verify product details page elements");

            // This method checks for:
            // 1. Product image is displayed
            // 2. Product price is visible
            // 3. "Add to Cart" button is present
            // 4. Product title matches (or is similar to) the expected title
            boolean detailsVerified = amazonFunctions.verifyProductDetailsPage(expectedTitle);

            // Assert that all details are verified
            Assert.assertTrue(detailsVerified,
                    "Product details page verification failed (Image, Price, or Add to Cart missing)");
            logger.info("Product details page verified successfully");

            logger.info("========================================");
            logger.info("TEST RESULT: PASSED ✓");
            logger.info("========================================");
            TestLogger.logTestEnd("TC_AMZ_004", "PASSED");

        } catch (AssertionError e) {
            logger.error("Test assertion failed: {}", e.getMessage());
            TestLogger.logTestEnd("TC_AMZ_004", "FAILED");
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            TestLogger.logTestEnd("TC_AMZ_004", "FAILED");
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
