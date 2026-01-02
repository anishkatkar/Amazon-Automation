import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.*;
import utils.ScreenshotListener;

/**
 * TC_AMZ_002: Verify Search with Invalid Keyword (Negative Test)
 * Priority: MEDIUM | Type: Negative Test | Automation: YES
 */
public class testcase2 {

    private WebDriver driver;
    private AmazonSearchFunctions amazonFunctions;

    // Test Data
    private static final String AMAZON_URL = "https://www.amazon.com";
    private static final String INVALID_KEYWORD = "xyzabc123!@#$%";

    @BeforeClass
    public void setupClass() {
        System.out.println("=".repeat(80));
        System.out.println("TC_AMZ_002: Verify Search with Invalid Keyword");
        System.out.println("Priority: MEDIUM | Type: Negative Test | Automation: YES");
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

    @Test(priority = 1, description = "Verify search behavior with invalid keywords")
    public void testInvalidKeywordSearch() {
        System.out.println("\nEXECUTING: TC_AMZ_002 - Verify Invalid Search");

        // Step 1: Navigate to Amazon
        amazonFunctions.navigateToAmazonHomepage(AMAZON_URL);

        // Step 2 & 3: Enter invalid keyword
        WebElement searchBox = amazonFunctions.locateSearchBox();
        amazonFunctions.clearSearchBox(searchBox);
        amazonFunctions.enterSearchKeyword(searchBox, INVALID_KEYWORD);

        // Step 4: Click search
        WebElement searchButton = amazonFunctions.locateSearchButton();
        amazonFunctions.clickSearchButton(searchButton);
        amazonFunctions.waitForSearchResults();

        // Step 6 & 7: Check for "no results" OR suggestions
        boolean noResultsMsg = amazonFunctions.verifyNoResultsMessage();
        boolean hasSuggestions = amazonFunctions.verifySuggestionsDisplayed();

        System.out.println("No Results Message Found: " + noResultsMsg);
        System.out.println("Suggestions Found: " + hasSuggestions);

        if (!noResultsMsg && !hasSuggestions) {
            System.out.println("xxxxx Method testInvalidKeywordSearch failed xxxxx");
            System.out.println("Taking screenshot...");
            String screenshotPath = ScreenshotListener.captureScreenshot(driver, "testInvalidKeywordSearch_DEBUG");
            System.out.println("Screenshot saved successfully at: " + screenshotPath);
        }

        Assert.assertTrue(noResultsMsg || hasSuggestions,
                "Expected either 'No results' message or suggestions to be displayed");

        // Step 8: Verify no product cards
        int productCount = amazonFunctions.countProductCards();
        Assert.assertEquals(productCount, 0, "Expected 0 results for invalid keyword but found " + productCount);

        System.out.println("TEST RESULT: PASSED ✓");
    }

    @AfterMethod
    public void teardown() {
        if (driver != null) {
            // driver.quit(); // Keep open for review if needed
        }
    }

    @AfterClass
    public void cleanupClass() {
        if (driver != null) {
            driver.quit();
        }
    }
}
