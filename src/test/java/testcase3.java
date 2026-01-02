import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.*;
import java.util.List;

/**
 * TC_AMZ_003: Verify Search with Price Filter
 * Priority: HIGH | Type: Functional Test | Automation: YES
 */
public class testcase3 {

    private WebDriver driver;
    private AmazonSearchFunctions amazonFunctions;

    // Test Data
    private static final String AMAZON_URL = "https://www.amazon.com";
    private static final String SEARCH_KEYWORD = "laptop";
    private static final double MIN_PRICE = 500.0;
    private static final double MAX_PRICE = 1000.0;

    @BeforeClass
    public void setupClass() {
        System.out.println("=".repeat(80));
        System.out.println("TC_AMZ_003: Verify Search with Price Filter");
        System.out.println("Priority: HIGH | Type: Functional Test | Automation: YES");
        System.out.println("=".repeat(80));
        WebDriverManager.chromedriver().setup();
    }

    @BeforeMethod
    public void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        driver = new ChromeDriver(options);
        amazonFunctions = new AmazonSearchFunctions(driver);
    }

    @Test(priority = 1, description = "Verify search results can be filtered by price")
    public void testPriceFilter() {
        System.out.println("\nEXECUTING: TC_AMZ_003 - Verify Price Filter");

        // 1. Navigate & Search "laptop"
        amazonFunctions.navigateToAmazonHomepage(AMAZON_URL);
        WebElement searchBox = amazonFunctions.locateSearchBox();
        amazonFunctions.clearSearchBox(searchBox);
        amazonFunctions.enterSearchKeyword(searchBox, SEARCH_KEYWORD);
        WebElement searchButton = amazonFunctions.locateSearchButton();
        amazonFunctions.clickSearchButton(searchButton);
        amazonFunctions.waitForSearchResults();

        // 2. Apply Price Filter ($500 - $1000)
        boolean filterClicked = amazonFunctions.applyPriceFilter500to1000();
        Assert.assertTrue(filterClicked, "Failed to apply price filter");

        // Wait for results to reload
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        } // Helper wait for safety
        amazonFunctions.waitForSearchResults();

        // 3. Extract Prices
        List<Double> prices = amazonFunctions.getAllProductPrices();
        Assert.assertFalse(prices.isEmpty(), "No price elements found after filtering");

        // 4. Validate Price Range
        boolean allInRange = amazonFunctions.verifyPricesInRange(prices, MIN_PRICE, MAX_PRICE);
        Assert.assertTrue(allInRange,
                "Found products outside the expected price range $" + MIN_PRICE + "-" + MAX_PRICE);

        System.out.println("TEST RESULT: PASSED ✓");
    }

    @AfterMethod
    public void teardown() {
        if (driver != null) {
            // driver.quit();
        }
    }

    @AfterClass
    public void cleanupClass() {
        if (driver != null) {
            driver.quit();
        }
    }
}
