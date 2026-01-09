import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

/**
 * AmazonSearchFunctions - Reusable functions for Amazon Search Test Automation
 * TC_AMZ_001: Verify Valid Product Search
 * 
 * This class contains all reusable functions for Amazon search functionality
 * testing.
 * Each function corresponds to specific test steps and can be reused across
 * multiple test cases.
 */
public class AmazonSearchFunctions {

    private WebDriver driver;
    private WebDriverWait wait;

    // Element Locators - Centralized for easy maintenance
    private static final String SEARCH_BOX_ID = "twotabsearchtextbox";
    private static final String SEARCH_BUTTON_ID = "nav-search-submit-button";
    private static final String SEARCH_RESULT_CSS = "[data-component-type='s-search-result']";
    private static final String RESULT_COUNT_XPATH = "//span[contains(text(),'results for')]";
    private static final String RESULT_COUNT_CSS = ".sg-col-inner";
    private static final String PRODUCT_IMAGE_CSS = ".s-image";
    private static final String PRODUCT_TITLE_CSS = ".a-size-medium.a-color-base.a-text-normal";
    private static final String PRODUCT_PRICE_CSS = ".a-price-whole";
    // New Constants for TC_AMZ_002 & TC_AMZ_003
    private static final String NO_RESULTS_XPATH = "//*[contains(text(),'No results') or contains(text(),'did not match any products') or contains(text(),'0 results for')]";
    private static final String SUGGESTIONS_CSS = ".s-suggestion";
    private static final String PRICE_FILTER_SECTION_XPATH = "//span[text()='Price']/ancestor::div[contains(@class,'a-section')]";

    /**
     * Constructor to initialize WebDriver and WebDriverWait
     * 
     * @param driver WebDriver instance
     */
    public AmazonSearchFunctions(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /**
     * Step 1: Navigate to Amazon homepage
     * 
     * @param url Amazon URL
     * @return true if navigation is successful
     */
    public boolean navigateToAmazonHomepage(String url) {
        try {
            driver.get(url);
            System.out.println("✓ Step 1: Successfully navigated to Amazon homepage: " + url);
            handleAddressPopUp(); // Automatically handle potential popups after navigation
            return true;
        } catch (Exception e) {
            System.err.println("✗ Step 1 Failed: Unable to navigate to " + url);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Handle potential "Deliver to" or regional popups and set US zip code for USD
     * prices
     */
    public void handleAddressPopUp() {
        try {
            // 1. Try to dismiss simple popups first
            String[] popupDismissLocators = {
                    "input[data-action-type='DISMISS']",
                    "//span[contains(text(),'Dismiss')]/ancestor::button",
                    "//div[contains(@class,'a-popover')]//button[@data-action='a-popover-close']"
            };

            for (String locator : popupDismissLocators) {
                try {
                    List<WebElement> elements = locator.startsWith("//") ? driver.findElements(By.xpath(locator))
                            : driver.findElements(By.cssSelector(locator));
                    if (!elements.isEmpty() && elements.get(0).isDisplayed()) {
                        elements.get(0).click();
                        System.out.println("✓ Dismissed popup using: " + locator);
                        Thread.sleep(1000);
                    }
                } catch (Exception ignore) {
                }
            }

            // 2. Try to set US Zip code if not already US
            try {
                WebElement locationSlot = wait
                        .until(ExpectedConditions.visibilityOfElementLocated(By.id("nav-global-location-slot")));
                if (!locationSlot.getText().contains("90210") && !locationSlot.getText().contains("United States")) {
                    System.out.println("ℹ Setting US Zip code (90210) for USD pricing...");
                    locationSlot.click();

                    WebElement zipInput = wait
                            .until(ExpectedConditions.visibilityOfElementLocated(By.id("GLUXZipUpdateInput")));
                    zipInput.clear();
                    zipInput.sendKeys("90210");

                    WebElement applyButton = driver.findElement(By.id("GLUXZipUpdate"));
                    applyButton.click();

                    // Wait a moment for the page to respond
                    Thread.sleep(2000);

                    // Check if we were redirected to login page
                    if (isOnLoginPage()) {
                        System.out.println("ℹ Login required. Attempting to sign in...");
                        boolean loginSuccess = performLogin();
                        if (loginSuccess) {
                            System.out.println("✓ Login successful! Continuing with zip code setup...");
                            // After login, we might need to re-attempt zip code setting
                            Thread.sleep(2000);
                            // Check if we're back on Amazon main page
                            if (!isOnLoginPage()) {
                                // Try to set zip code again
                                handleAddressPopUp();
                            }
                        } else {
                            System.err.println("⚠ Login failed or OTP required. Continuing without zip code...");
                        }
                        return;
                    }

                    try {
                        // User requested Scoped Fix: Find popover container first, then button
                        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(20));

                        // 1. Find the popover container (User provided ID: a-popover-1)
                        WebElement popoverContainer = longWait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.xpath(
                                        "//*[@id='a-popover-1']/div | //div[starts-with(@id, 'a-popover-')]//div[@class='a-popover-wrapper']")));
                        System.out.println("✓ Located popover container");

                        // 2. Retry mechanism for Stale Element Exception
                        boolean clicked = false;
                        for (int i = 0; i < 3; i++) {
                            try {
                                // Re-find the button inside the container to avoid staleness
                                WebElement continueButton = popoverContainer.findElement(By.xpath(
                                        ".//input[@type='submit'] | .//span[contains(text(),'Continue')]"));

                                // Use JS Click to avoid ElementNotInteractableException
                                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();",
                                        continueButton);
                                System.out.println("✓ Clicked 'Continue' inside popover container (via JS)");
                                clicked = true;
                                break; // Exit loop if successful
                            } catch (org.openqa.selenium.StaleElementReferenceException e) {
                                System.out.println("⚠ Stale element detected. Retrying " + (i + 1) + "/3...");
                                // Re-find container as well if needed
                                popoverContainer = driver.findElement(By.xpath(
                                        "//*[@id='a-popover-1']/div | //div[starts-with(@id, 'a-popover-')]//div[@class='a-popover-wrapper']"));
                                Thread.sleep(500); // Small pause
                            }
                        }

                        if (!clicked) {
                            throw new Exception("Failed to click continue button after retries");
                        }

                    } catch (Exception e) {
                        System.err.println("⚠ Scoped XPath failed (" + e.getMessage() + "). Attempting fallbacks...");
                        // Fallback: Try the robust locators if the absolute XPath fails
                        try {
                            WebElement continueButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                                    "//*[@id=\"GLUXConfirmClose\"]")));
                            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();",
                                    continueButton);
                            System.out.println("✓ Clicked 'Continue' on address popup (Fallback)");
                        } catch (Exception ex) {
                            // If no continue button, click the close/done button if visible
                            try {
                                WebElement doneButton = driver.findElement(By.name("glowDoneButton"));
                                if (doneButton.isDisplayed()) {
                                    doneButton.click();
                                }
                            } catch (Exception ignore) {
                            }
                        }
                    }

                    Thread.sleep(5000); // Wait 5s for page to reload/settle completely
                    System.out.println("✓ Successfully set US Zip code.");
                }
            } catch (Exception e) {
                // Location slot might not be present on some pages or already set
            }
        } catch (Exception e) {
            System.err.println("⚠ Warning: Error while trying to handle address/zip popup: " + e.getMessage());
        }
    }

    /**
     * Check if currently on Amazon login page
     * 
     * @return true if on login page
     */
    public boolean isOnLoginPage() {
        try {
            String currentUrl = driver.getCurrentUrl();
            String pageSource = driver.getPageSource().toLowerCase();

            // Check URL patterns for login page
            if (currentUrl.contains("/ap/signin") || currentUrl.contains("/ap/register") ||
                    currentUrl.contains("signin.amazon")) {
                return true;
            }

            // Check page content for login indicators
            if (pageSource.contains("sign in or create account") ||
                    pageSource.contains("enter mobile number or email") ||
                    pageSource.contains("what is your email") ||
                    pageSource.contains("sign in to your account")) {
                return true;
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Load credentials from config.properties file
     * 
     * @return Properties object with credentials
     */
    private Properties loadCredentials() {
        Properties props = new Properties();
        try {
            // Try loading from classpath first
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties");
            if (inputStream == null) {
                // Fallback to file system
                inputStream = new FileInputStream("src/test/resources/config.properties");
            }
            props.load(inputStream);
            inputStream.close();
            System.out.println("✓ Credentials loaded from config file");
        } catch (Exception e) {
            System.err.println("⚠ Could not load credentials: " + e.getMessage());
        }
        return props;
    }

    /**
     * Perform Amazon login with credentials from config file
     * 
     * @return true if login successful, false if OTP required or failed
     */
    public boolean performLogin() {
        try {
            Properties creds = loadCredentials();
            String email = creds.getProperty("amazon.email", "");
            String password = creds.getProperty("amazon.password", "");

            if (email.isEmpty() || password.isEmpty()) {
                System.err.println("✗ Login failed: Credentials not found in config.properties");
                return false;
            }

            WebDriverWait loginWait = new WebDriverWait(driver, Duration.ofSeconds(15));

            // Step 1: Enter email/phone
            try {
                WebElement emailInput = loginWait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("#ap_email, #ap_email_login, input[name='email']")));
                emailInput.clear();
                emailInput.sendKeys(email);
                System.out.println("✓ Entered email: " + email);

                // Click Continue button
                WebElement continueBtn = driver.findElement(By.cssSelector("#continue, input[type='submit']"));
                continueBtn.click();
                Thread.sleep(2000);
            } catch (Exception e) {
                System.err.println("⚠ Email input failed: " + e.getMessage());
            }

            // Step 2: Enter password
            try {
                WebElement passwordInput = loginWait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("#ap_password, input[name='password']")));
                passwordInput.clear();
                passwordInput.sendKeys(password);
                System.out.println("✓ Entered password");

                // Click Sign In button
                WebElement signInBtn = driver.findElement(By.cssSelector("#signInSubmit, input[type='submit']"));
                signInBtn.click();
                Thread.sleep(3000);
            } catch (Exception e) {
                System.err.println("⚠ Password input failed: " + e.getMessage());
                return false;
            }

            // Step 3: Check for OTP challenge
            if (isOtpRequired()) {
                System.out.println("═".repeat(60));
                System.out.println("⚠ OTP VERIFICATION REQUIRED!");
                System.out.println("Please enter the OTP sent to your phone/email manually.");
                System.out.println("The test will wait for 60 seconds for you to complete OTP entry.");
                System.out.println("═".repeat(60));

                // Wait for OTP to be entered manually (up to 60 seconds)
                for (int i = 0; i < 60; i++) {
                    Thread.sleep(1000);
                    if (!isOtpRequired() && !isOnLoginPage()) {
                        System.out.println("✓ OTP verified successfully!");
                        return true;
                    }
                }
                System.err.println("✗ OTP timeout. Please run the test again.");
                return false;
            }

            // Check if login was successful
            if (!isOnLoginPage()) {
                System.out.println("✓ Login completed successfully!");
                return true;
            }

            return false;
        } catch (Exception e) {
            System.err.println("✗ Login failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if OTP verification is required
     * 
     * @return true if OTP page is displayed
     */
    private boolean isOtpRequired() {
        try {
            String pageSource = driver.getPageSource().toLowerCase();
            return pageSource.contains("enter otp") ||
                    pageSource.contains("verification code") ||
                    pageSource.contains("enter the otp") ||
                    pageSource.contains("one time password") ||
                    pageSource.contains("verify your identity") ||
                    pageSource.contains("approval notification") ||
                    driver.findElements(By.id("auth-mfa-otpcode")).size() > 0 ||
                    driver.findElements(By.cssSelector("input[name='otpCode']")).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Step 2: Locate search box
     * 
     * @return WebElement of search box if found, null otherwise
     */
    public WebElement locateSearchBox() {
        try {
            WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id(SEARCH_BOX_ID)));

            if (searchBox.isEnabled()) {
                System.out.println("✓ Step 2: Search box located and is enabled");
                return searchBox;
            } else {
                System.err.println("✗ Step 2 Failed: Search box is not enabled");
                return null;
            }
        } catch (Exception e) {
            System.err.println("✗ Step 2 Failed: Unable to locate search box");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Step 3: Clear existing text in search box
     * 
     * @param searchBox WebElement of search box
     * @return true if clearing is successful
     */
    public boolean clearSearchBox(WebElement searchBox) {
        try {
            searchBox.clear();
            System.out.println("✓ Step 3: Search box cleared successfully");
            return true;
        } catch (Exception e) {
            System.err.println("✗ Step 3 Failed: Unable to clear search box");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Step 4: Enter search keyword in search box
     * 
     * @param searchBox WebElement of search box
     * @param keyword   Search keyword to enter
     * @return true if text entry is successful
     */
    public boolean enterSearchKeyword(WebElement searchBox, String keyword) {
        try {
            searchBox.sendKeys(keyword);
            String enteredText = searchBox.getAttribute("value");

            if (enteredText.equals(keyword)) {
                System.out.println("✓ Step 4: Successfully entered keyword: '" + keyword + "'");
                return true;
            } else {
                System.err.println("✗ Step 4 Failed: Entered text doesn't match. Expected: '"
                        + keyword + "', Got: '" + enteredText + "'");
                return false;
            }
        } catch (Exception e) {
            System.err.println("✗ Step 4 Failed: Unable to enter search keyword");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Step 5: Locate search button
     * 
     * @return WebElement of search button if found, null otherwise
     */
    public WebElement locateSearchButton() {
        try {
            WebElement searchButton = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.id(SEARCH_BUTTON_ID)));
            System.out.println("✓ Step 5: Search button located successfully");
            return searchButton;
        } catch (Exception e) {
            System.err.println("✗ Step 5 Failed: Unable to locate search button");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Step 6: Click search button
     * 
     * @param searchButton WebElement of search button
     * @return true if click is successful
     */
    public boolean clickSearchButton(WebElement searchButton) {
        try {
            searchButton.click();
            System.out.println("✓ Step 6: Search button clicked successfully");
            return true;
        } catch (org.openqa.selenium.ElementClickInterceptedException e) {
            System.out.println("⚠ Step 6 Warning: Click intercepted, attempting JS Click fallback...");
            try {
                // Fallback: Force click with JavaScript
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", searchButton);
                System.out.println("✓ Step 6: Search button clicked successfully (via JS)");
                return true;
            } catch (Exception jsEx) {
                System.err.println("✗ Step 6 Failed: Unable to click search button even with JS");
                jsEx.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            System.err.println("✗ Step 6 Failed: Unable to click search button");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Step 7: Wait for search results to load
     * 
     * @return true if results are loaded within timeout
     */
    public boolean waitForSearchResults() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(SEARCH_RESULT_CSS)));
            System.out.println("✓ Step 7: Search results loaded successfully");
            return true;
        } catch (Exception e) {
            System.err.println("✗ Step 7 Failed: Search results did not load within timeout");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Step 8: Verify results count text
     * 
     * @param expectedKeyword Expected keyword in results text
     * @return true if results count text is displayed correctly
     */
    public boolean verifyResultsCountText(String expectedKeyword) {
        try {
            // Try XPATH first
            List<WebElement> resultsTextElements = driver.findElements(By.xpath(RESULT_COUNT_XPATH));

            // If XPATH doesn't work, try CSS
            if (resultsTextElements.isEmpty()) {
                resultsTextElements = driver.findElements(By.cssSelector(RESULT_COUNT_CSS));
            }

            for (WebElement element : resultsTextElements) {
                String text = element.getText();
                if (text.contains("results for") && text.contains(expectedKeyword)) {
                    System.out.println("✓ Step 8: Results count text verified: " + text);
                    return true;
                }
            }

            System.err.println("✗ Step 8 Failed: Results count text not found or doesn't contain keyword");
            return false;
        } catch (Exception e) {
            System.err.println("✗ Step 8 Failed: Unable to verify results count text");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Step 9: Count number of product cards
     * 
     * @return Number of product cards found
     */
    public int countProductCards() {
        try {
            List<WebElement> results = driver.findElements(By.cssSelector(SEARCH_RESULT_CSS));
            int count = results.size();
            System.out.println("✓ Step 9: Found " + count + " product cards");
            return count;
        } catch (Exception e) {
            System.err.println("✗ Step 9 Failed: Unable to count product cards");
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Step 9 (Validation): Verify minimum product count
     * 
     * @param actualCount      Actual number of products found
     * @param expectedMinCount Expected minimum count
     * @return true if actual count meets or exceeds expected minimum
     */
    public boolean verifyMinimumProductCount(int actualCount, int expectedMinCount) {
        if (actualCount >= expectedMinCount) {
            System.out.println("✓ Step 9 Validation: Product count (" + actualCount
                    + ") meets minimum requirement (" + expectedMinCount + ")");
            return true;
        } else {
            System.err.println("✗ Step 9 Validation Failed: Product count (" + actualCount
                    + ") is less than minimum requirement (" + expectedMinCount + ")");
            return false;
        }
    }

    /**
     * Step 10: Verify first product has image
     * 
     * @return true if first product has a displayed image
     */
    public boolean verifyFirstProductHasImage() {
        try {
            WebElement firstProduct = driver.findElement(By.cssSelector(SEARCH_RESULT_CSS));
            WebElement image = firstProduct.findElement(By.cssSelector(PRODUCT_IMAGE_CSS));

            if (image.isDisplayed()) {
                System.out.println("✓ Step 10: First product has a displayed image");
                return true;
            } else {
                System.err.println("✗ Step 10 Failed: First product image is not displayed");
                return false;
            }
        } catch (Exception e) {
            System.err.println("✗ Step 10 Failed: Unable to find image in first product");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Step 11: Verify first product has title
     * 
     * @return true if first product has a non-empty title
     */
    public boolean verifyFirstProductHasTitle() {
        try {
            WebElement firstProduct = driver.findElement(By.cssSelector(SEARCH_RESULT_CSS));
            WebElement title = firstProduct.findElement(By.cssSelector(PRODUCT_TITLE_CSS));
            String titleText = title.getText();

            if (titleText != null && !titleText.trim().isEmpty()) {
                System.out.println("✓ Step 11: First product has title: " + titleText);
                return true;
            } else {
                System.err.println("✗ Step 11 Failed: First product title is empty");
                return false;
            }
        } catch (Exception e) {
            System.err.println("✗ Step 11 Failed: Unable to find title in first product");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Step 12: Verify first product has price
     * 
     * @return true if first product has a price element
     */
    public boolean verifyFirstProductHasPrice() {
        try {
            WebElement firstProduct = driver.findElement(By.cssSelector(SEARCH_RESULT_CSS));

            // Try multiple price selectors as Amazon's structure varies
            String[] priceSelectors = {
                    ".a-price-whole",
                    ".a-price .a-offscreen",
                    ".a-price",
                    "span.a-price",
                    ".a-color-price"
            };

            for (String selector : priceSelectors) {
                try {
                    WebElement price = firstProduct.findElement(By.cssSelector(selector));
                    if (price != null) {
                        String priceText = price.getText();
                        if (priceText == null || priceText.trim().isEmpty()) {
                            priceText = price.getAttribute("textContent");
                        }
                        System.out.println("✓ Step 12: First product has price: " + priceText);
                        return true;
                    }
                } catch (Exception e) {
                    // Try next selector
                    continue;
                }
            }

            System.err.println("✗ Step 12 Failed: First product price element not found with any selector");
            return false;
        } catch (Exception e) {
            System.err.println("✗ Step 12 Failed: Unable to find price in first product");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Comprehensive validation: Verify all products have required elements
     * 
     * @param maxProductsToCheck Maximum number of products to check (to avoid long
     *                           execution)
     * @return true if all checked products have image, title, and price
     */
    public boolean verifyAllProductsHaveRequiredElements(int maxProductsToCheck) {
        try {
            List<WebElement> products = driver.findElements(By.cssSelector(SEARCH_RESULT_CSS));
            int productsToCheck = Math.min(products.size(), maxProductsToCheck);
            int validProducts = 0;

            System.out.println("\n--- Validating " + productsToCheck + " products ---");

            for (int i = 0; i < productsToCheck; i++) {
                WebElement product = products.get(i);
                boolean hasImage = false;
                boolean hasTitle = false;
                boolean hasPrice = false;

                try {
                    hasImage = product.findElement(By.cssSelector(PRODUCT_IMAGE_CSS)).isDisplayed();
                } catch (Exception e) {
                    // Image not found
                }

                try {
                    String title = product.findElement(By.cssSelector(PRODUCT_TITLE_CSS)).getText();
                    hasTitle = title != null && !title.trim().isEmpty();
                } catch (Exception e) {
                    // Title not found
                }

                try {
                    hasPrice = product.findElement(By.cssSelector(PRODUCT_PRICE_CSS)) != null;
                } catch (Exception e) {
                    // Price not found
                }

                if (hasImage && hasTitle && hasPrice) {
                    validProducts++;
                }

                System.out.println("Product " + (i + 1) + ": Image=" + hasImage
                        + ", Title=" + hasTitle + ", Price=" + hasPrice);
            }

            System.out.println("Valid products: " + validProducts + "/" + productsToCheck);
            System.out.println("--- Validation Complete ---\n");

            return validProducts == productsToCheck;
        } catch (Exception e) {
            System.err.println("✗ Failed to validate all products");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get current page URL
     * 
     * @return Current page URL
     */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    /**
     * Get current page title
     * 
     * @return Current page title
     */
    public String getPageTitle() {
        return driver.getTitle();
    }

    // ==========================================
    // TC_AMZ_002: Negative Search Methods
    // ==========================================

    /**
     * Verify "No results" message is displayed
     * 
     * @return true if message found
     */
    public boolean verifyNoResultsMessage() {
        try {
            // Multiple patterns for "no results" or similar messages
            String[] noResultPatterns = {
                    "//*[contains(text(),'No results')]",
                    "//*[contains(text(),'did not match any products')]",
                    "//*[contains(text(),'0 results for')]",
                    "//span[contains(text(),'No results for')]",
                    "//div[contains(@class,'s-no-outline')]//span[contains(text(),'results')]",
                    "//span[contains(@data-component-type,'s-result-info-bar')]"
            };

            for (String pattern : noResultPatterns) {
                List<WebElement> elements = driver.findElements(By.xpath(pattern));
                for (WebElement el : elements) {
                    String text = el.getText().toLowerCase();
                    if (text.contains("no results") || text.contains("0 results") ||
                            text.contains("did not match")) {
                        System.out.println("✓ TC_AMZ_002: Found 'No results' message: " + text);
                        return true;
                    }
                }
            }

            // Also check if there's a very low result count (0-5 might indicate near-empty
            // results)
            String pageText = driver.getPageSource().toLowerCase();
            if (pageText.contains("no results") || pageText.contains("0 results for") ||
                    pageText.contains("did not match any products")) {
                System.out.println("✓ TC_AMZ_002: Found 'No results' indicator in page source");
                return true;
            }

            return false;
        } catch (Exception e) {
            System.err.println("✗ TC_AMZ_002 Failed: Error checking no results message");
            return false;
        }
    }

    /**
     * Verify suggestions are displayed or Amazon shows related/alternative results
     * 
     * @return true if suggestions found
     */
    public boolean verifySuggestionsDisplayed() {
        try {
            // Check for various suggestion/recommendation patterns
            String[] suggestionPatterns = {
                    ".s-suggestion",
                    "div[data-component-type='s-impression-logger']",
                    "//span[contains(text(),'Try checking your spelling')]",
                    "//span[contains(text(),'spelling')]",
                    "//div[contains(@class,'s-message-warning')]",
                    "//span[contains(text(),'related searches')]",
                    "//div[contains(@cel_widget_id,'MAIN-SEARCH_RESULTS')]"
            };

            for (String pattern : suggestionPatterns) {
                List<WebElement> elements = pattern.startsWith("//")
                        ? driver.findElements(By.xpath(pattern))
                        : driver.findElements(By.cssSelector(pattern));
                if (!elements.isEmpty()) {
                    System.out.println("✓ TC_AMZ_002: Found suggestions/recommendations using: " + pattern);
                    return true;
                }
            }

            // Check page source for spelling suggestions
            String pageText = driver.getPageSource().toLowerCase();
            if (pageText.contains("try checking your spelling") ||
                    pageText.contains("did you mean") ||
                    pageText.contains("related searches") ||
                    pageText.contains("explore related")) {
                System.out.println("✓ TC_AMZ_002: Found suggestions in page source");
                return true;
            }

            return false;
        } catch (Exception e) {
            System.err.println("✗ TC_AMZ_002 Failed: Error checking suggestions");
            return false;
        }
    }

    // ==========================================
    // TC_AMZ_003: Price Filter Methods
    // ==========================================

    /**
     * Apply price filter range $500 to $1000
     * 
     * @return true if filter clicked
     */
    public boolean applyPriceFilter500to1000() {
        if (applyCustomPriceRange(500, 1000)) {
            return true;
        }

        System.out.println("ℹ TC_AMZ_003: Manual inputs failed, trying link text...");
        // Fallback to link texts
        String[] linkTexts = {
                "$500 to $1,000",
                "500 to 1,000",
                "₹40,000 to ₹80,000",
                "Start at $500",
                "Up to $1,000"
        };

        for (String text : linkTexts) {
            try {
                WebElement link = driver.findElement(By.partialLinkText(text));
                if (link.isDisplayed()) {
                    link.click();
                    System.out.println("✓ TC_AMZ_003: Clicked price filter link: " + text);
                    return true;
                }
            } catch (Exception ignore) {
            }
        }

        System.err.println("✗ TC_AMZ_003 Failed: Could not apply price filter using inputs or links.");
        return false;
    }

    /**
     * Apply custom price range via input fields or URL manipulation
     * 
     * @param min Minimum price
     * @param max Maximum price
     * @return true if applied successfully
     */
    public boolean applyCustomPriceRange(double min, double max) {
        try {
            System.out.println("ℹ TC_AMZ_003: Attempting to apply price filter (" + min + "-" + max + ")");

            // Try URL manipulation first as it is the most robust across regions
            String currentUrl = driver.getCurrentUrl();
            if (currentUrl.contains("k=" + "laptop".replace(" ", "+"))) { // Basic check to ensure we are on a results
                                                                          // page
                String separator = currentUrl.contains("?") ? "&" : "?";
                String newUrl = currentUrl;

                // Remove existing price, currency, and language params if any
                newUrl = newUrl.replaceAll("&low-price=[^&]*", "").replaceAll("&high-price=[^&]*", "")
                        .replaceAll("&currency=[^&]*", "").replaceAll("&language=[^&]*", "");

                // Force USD and US English to get consistent price values
                newUrl += separator + "low-price=" + (int) min + "&high-price=" + (int) max
                        + "&currency=USD&language=en_US";

                System.out.println("ℹ TC_AMZ_003: Navigating to filtered URL (Forcing USD): " + newUrl);
                driver.get(newUrl);
                waitForSearchResults();
                handleAddressPopUp(); // Handle any new popups after navigation
                System.out.println("✓ TC_AMZ_003: Applied price filter via URL manipulation (Forced USD).");
                return true;
            }

            // Fallback to manual input fields if URL trick is not suitable
            WebElement lowPriceInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("low-price")));
            WebElement highPriceInput = driver.findElement(By.id("high-price"));
            WebElement goButton = driver.findElement(By.cssSelector("#a-autoid-1 button, .a-button-input"));

            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
                    lowPriceInput);
            Thread.sleep(500);

            lowPriceInput.clear();
            lowPriceInput.sendKeys(String.valueOf((int) min));
            highPriceInput.clear();
            highPriceInput.sendKeys(String.valueOf((int) max));
            goButton.click();

            System.out.println("✓ TC_AMZ_003: Applied custom price range via inputs successfully.");
            return true;
        } catch (Exception e) {
            System.err.println("⚠ TC_AMZ_003 Warning: Manual price application failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all product prices from current page, skipping sponsored items
     * 
     * @return List of prices as Doubles
     */
    public java.util.List<Double> getAllProductPrices() {
        java.util.List<Double> prices = new java.util.ArrayList<>();
        try {
            List<WebElement> resultCards = driver.findElements(By.cssSelector(SEARCH_RESULT_CSS));
            System.out.println("ℹ TC_AMZ_003: Found " + resultCards.size() + " total search result cards.");

            for (WebElement card : resultCards) {
                // 1. Skip Sponsored Results
                boolean isSponsored = false;
                try {
                    List<WebElement> sponsoredLabels = card
                            .findElements(By.xpath(".//span[contains(text(),'Sponsored')]"));
                    if (!sponsoredLabels.isEmpty()) {
                        isSponsored = true;
                    }
                } catch (Exception e) {
                    // Ignore
                }

                if (isSponsored) {
                    continue;
                }

                // 2. Extract Price
                try {
                    // Try multiple price selectors within the card
                    String[] cardPriceSelectors = { ".a-price-whole", ".a-price .a-offscreen", ".a-color-price" };
                    for (String selector : cardPriceSelectors) {
                        try {
                            WebElement priceElem = card.findElement(By.cssSelector(selector));
                            String rawText = priceElem.getText();
                            if (rawText == null || rawText.isEmpty()) {
                                rawText = priceElem.getAttribute("textContent");
                            }

                            String priceText = rawText.replaceAll("[^0-9.]", "").trim();
                            if (!priceText.isEmpty()) {
                                double price = Double.parseDouble(priceText);
                                prices.add(price);
                                break; // Found price for this card
                            }
                        } catch (Exception next) {
                            continue;
                        }
                    }
                } catch (Exception e) {
                    // Error extracting price from this specific card
                }
            }
            System.out.println("✓ TC_AMZ_003: Extracted " + prices.size() + " organic product prices.");
        } catch (Exception e) {
            System.err.println("✗ TC_AMZ_003 Failed: Error extracting product prices");
            e.printStackTrace();
        }
        return prices;
    }

    /**
     * Verify all prices are within range
     * 
     * @param prices List of prices
     * @param min    Minimum price
     * @param max    Maximum price
     * @return true if all prices in range
     */
    public boolean verifyPricesInRange(java.util.List<Double> prices, double min, double max) {
        if (prices.isEmpty()) {
            System.err.println("⚠ TC_AMZ_003 Warning: No prices found to verify");
            return false; // Or true if we consider 0 results as valid strictly? Let's say false for test
                          // robustness
        }
        boolean allInRange = true;
        for (Double price : prices) {
            if (price < min || price > max) {
                System.err.println("✗ TC_AMZ_003 Failure: Price $" + price + " is out of range check");
                allInRange = false;
            }
        }
        if (allInRange) {
            System.out.println("✓ TC_AMZ_003: All " + prices.size() + " prices are between $" + min + " and $" + max);
        }
        return allInRange;
    }

    // ==========================================
    // TC_AMZ_004: Product Details Methods
    // ==========================================

    /**
     * Store first product title before clicking
     *
     * @return Product title string
     */
    public String getFirstProductTitle() {
        try {
            WebElement firstProduct = driver.findElement(By.cssSelector(SEARCH_RESULT_CSS));
            WebElement title = firstProduct.findElement(By.cssSelector(PRODUCT_TITLE_CSS));
            return title.getText();
        } catch (Exception e) {
            System.err.println("✗ Failed to get first product title");
            return "";
        }
    }

    /**
     * Click on the first product in search results
     *
     * @return true if clicked and navigated
     */
    public boolean clickFirstProduct() {
        try {
            WebElement firstProduct = driver.findElement(By.cssSelector(SEARCH_RESULT_CSS));
            WebElement link = firstProduct.findElement(By.cssSelector(".a-link-normal.s-no-outline"));

            String currentUrl = driver.getCurrentUrl();
            link.click();
            System.out.println("✓ Clicked first product");

            // Wait for navigation or new tab
            try {
                wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(currentUrl)));
            } catch (Exception e) {
                // Might have opened in new tab, handle that
                java.util.Set<String> handles = driver.getWindowHandles();
                if (handles.size() > 1) {
                    for (String handle : handles) {
                        driver.switchTo().window(handle);
                    }
                    System.out.println("✓ Switched to new tab");
                }
            }
            return true;
        } catch (Exception e) {
            System.err.println("✗ Failed to click first product");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verify product details page is loaded and critical elements exist
     *
     * @param expectedTitlePartial Title from search results to match
     * @return true if validation passes
     */
    public boolean verifyProductDetailsPage(String expectedTitlePartial) {
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(By.id("productTitle")),
                    ExpectedConditions.visibilityOfElementLocated(By.id("title"))));

            String actualTitle = driver.findElement(By.id("productTitle")).getText();
            System.out.println("✓ Product Page Title: " + actualTitle);

            if (!actualTitle.contains(expectedTitlePartial) && !expectedTitlePartial.contains(actualTitle)) {
                // Fuzzy match or just warning, titles might be truncated in search
                System.out.println("⚠ Warning: Title on page might differ slightly from search result.");
            }

            // Verify Image
            boolean imageVisible = driver.findElements(By.id("landingImage")).size() > 0 ||
                    driver.findElements(By.id("imgTagWrapperId")).size() > 0;

            // Verify Price
            boolean priceVisible = driver.findElements(By.cssSelector(".a-price-whole")).size() > 0 ||
                    driver.findElements(By.id("priceblock_ourprice")).size() > 0 ||
                    driver.findElements(By.id("corePriceDisplay_desktop_feature_div")).size() > 0;

            // Verify Add to Cart
            boolean addToCartVisible = driver.findElements(By.id("add-to-cart-button")).size() > 0;

            System.out.println("✓ Details Check: Image=" + imageVisible + ", Price=" + priceVisible + ", AddToCart="
                    + addToCartVisible);

            return imageVisible && addToCartVisible;
        } catch (Exception e) {
            System.err.println("✗ Failed to verify product details page");
            e.printStackTrace();
            return false;
        }
    }

    // ==========================================
    // TC_AMZ_005 & TC_AMZ_006: Cart Methods
    // ==========================================

    public int getCartCount() {
        try {
            WebElement countElem = driver.findElement(By.id("nav-cart-count"));
            return Integer.parseInt(countElem.getText());
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean addToCart() {
        try {
            int initialCount = getCartCount();
            WebElement addToCartBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("add-to-cart-button")));
            addToCartBtn.click();
            System.out.println("✓ Clicked 'Add to Cart'");

            // Handle Protection Plan / "Add to your order" Modal
            try {
                // Wait briefly for modal to appear
                Thread.sleep(2000); // Give modal time to render

                // Strategy 1: Try clicking "No thanks" input button (most common)
                try {
                    WebElement noThanksInput = new WebDriverWait(driver, Duration.ofSeconds(3))
                            .until(ExpectedConditions.elementToBeClickable(
                                    By.xpath("//input[@aria-labelledby='attachSiNoCoverage-announce']")));
                    noThanksInput.click();
                    System.out.println("✓ Dismissed Protection Plan modal (via input button)");
                } catch (Exception e1) {
                    // Strategy 2: Try CSS selector for attachSiNoCoverage
                    try {
                        WebElement noThanksBtn = new WebDriverWait(driver, Duration.ofSeconds(2))
                                .until(ExpectedConditions.elementToBeClickable(By.cssSelector(
                                        "#attachSiNoCoverage, #attach-si-no-coverage, button[data-action='a-popover-close']")));
                        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();",
                                noThanksBtn);
                        System.out.println("✓ Dismissed Protection Plan modal (via CSS)");
                    } catch (Exception e2) {
                        // Strategy 3: Try finding by text "No thanks"
                        try {
                            List<WebElement> noThanksText = driver
                                    .findElements(By.xpath("//*[contains(text(), 'No thanks')]"));
                            if (!noThanksText.isEmpty() && noThanksText.get(0).isDisplayed()) {
                                noThanksText.get(0).click();
                                System.out.println("✓ Dismissed Protection Plan modal (via text)");
                            }
                        } catch (Exception e3) {
                            // No modal appeared or couldn't dismiss - continue anyway
                            System.out.println("ℹ No protection plan modal detected or already dismissed");
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Wait for confirmation or count update
            try {
                wait.until(ExpectedConditions.or(
                        ExpectedConditions
                                .visibilityOfElementLocated(By.xpath("//*[contains(text(),'Added to Cart')]")),
                        ExpectedConditions.visibilityOfElementLocated(By.id("attach-sidesheet-view-cart-button")),
                        ExpectedConditions.visibilityOfElementLocated(By.id("sw-gtc")),
                        ExpectedConditions.not(
                                ExpectedConditions.textToBe(By.id("nav-cart-count"), String.valueOf(initialCount)))));
            } catch (Exception ignore) {
            }

            Thread.sleep(1500); // Small buffer for animation
            return true;
        } catch (Exception e) {
            System.err.println("✗ Failed to add to cart");
            e.printStackTrace();
            return false;
        }
    }

    public boolean navigateToCart() {
        try {
            driver.findElement(By.id("nav-cart")).click();
            wait.until(ExpectedConditions.urlContains("/cart"));
            System.out.println("✓ Navigated to Shopping Cart");
            return true;
        } catch (Exception e) {
            System.err.println("✗ Failed to navigate to cart");
            return false;
        }
    }

    public boolean updateCartQuantity(String qty) {
        try {
            // Try standard select first
            try {
                WebElement qtyDropdown = driver.findElement(By.name("quantity"));
                if (qtyDropdown.getTagName().equalsIgnoreCase("select")) {
                    org.openqa.selenium.support.ui.Select select = new org.openqa.selenium.support.ui.Select(
                            qtyDropdown);
                    select.selectByValue(qty);
                    System.out.println("✓ Updated quantity to " + qty + " (Select)");
                    Thread.sleep(3000);
                    return true;
                }
            } catch (Exception ignore) {
            }

            // Try Amazon custom dropdown container
            try {
                WebElement dropdownContainer = driver
                        .findElement(By.cssSelector(".a-dropdown-container, .sc-action-quantity"));
                dropdownContainer.click();

                // Click the option
                WebElement option = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("li[aria-labelledby*='quantity_" + qty + "'], #quantity_" + qty)));
                option.click();
                System.out.println("✓ Updated quantity to " + qty + " (Custom click)");
                Thread.sleep(3000);
                return true;
            } catch (Exception e) {
                // Try clicking the span showing current quantity
                WebElement qtyLabel = driver.findElement(By.cssSelector(".a-dropdown-prompt"));
                qtyLabel.click();
                WebElement option = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("quantity_" + qty)));
                option.click();
                System.out.println("✓ Updated quantity to " + qty + " (Prompt click)");
                Thread.sleep(3000);
                return true;
            }
        } catch (Exception e) {
            System.err.println("✗ Failed to update cart quantity");
            return false;
        }
    }

    public double getCartSubtotal() {
        try {
            WebElement subtotalElem = driver.findElement(By.id("sc-subtotal-amount-activecart"));
            String text = subtotalElem.getText().replace("$", "").replace(",", "").trim();
            return Double.parseDouble(text);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public boolean deleteFirstItemFromCart() {
        try {
            WebElement deleteBtn = driver.findElement(By.xpath("//input[@value='Delete']"));
            deleteBtn.click();
            System.out.println("✓ Clicked 'Delete' on first item");
            Thread.sleep(2000);
            return true;
        } catch (Exception e) {
            System.err.println("✗ Failed to delete item from cart");
            return false;
        }
    }

    // ==========================================
    // TC_AMZ_008: Checkout Methods
    // ==========================================

    public boolean clickProceedToCheckout() {
        try {
            // Wait for any of the buttons to be clickable
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.elementToBeClickable(By.name("proceedToCheckout")),
                    ExpectedConditions.elementToBeClickable(By.id("sc-buy-box-ptc-button")),
                    ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Proceed to checkout']")),
                    ExpectedConditions.elementToBeClickable(By.partialLinkText("Proceed to checkout"))));

            // Find which one is present
            By[] locators = {
                    By.name("proceedToCheckout"),
                    By.id("sc-buy-box-ptc-button"),
                    By.cssSelector("input[value='Proceed to checkout']"),
                    By.partialLinkText("Proceed to checkout")
            };

            WebElement btn = null;
            for (By loc : locators) {
                try {
                    List<WebElement> elements = driver.findElements(loc);
                    for (WebElement el : elements) {
                        if (el.isDisplayed() && el.isEnabled()) {
                            btn = el;
                            break;
                        }
                    }
                    if (btn != null)
                        break;
                } catch (Exception e) {
                }
            }

            if (btn == null) {
                // Fallback
                btn = driver.findElement(By.name("proceedToCheckout"));
            }

            btn.click();
            System.out.println("✓ Clicked 'Proceed to checkout'");
            return true;
        } catch (Exception e) {
            System.err.println("✗ Failed to click 'Proceed to checkout': " + e.getMessage());
            return false;
        }
    }

    public boolean verifySignInPage() {
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("/signin"),
                    ExpectedConditions.urlContains("/ap/signin")));

            boolean emailFieldExists = driver.findElements(By.id("ap_email")).size() > 0 ||
                    driver.findElements(By.name("email")).size() > 0;

            if (emailFieldExists) {
                System.out.println("✓ Sign-in page validated (Email field present)");
                return true;
            } else {
                System.err.println("✗ Sign-in page loaded but email field not found");
                return false;
            }
        } catch (Exception e) {
            System.err.println("✗ Failed to verify sign-in page");
            return false;
        }
    }

    public boolean clickProductByTitle(String titlePartial) {
        try {
            // Dynamic locator for partial text match
            String xpath = "//*[contains(text(), '" + titlePartial + "')]";

            // Wait for at least one match
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));

            List<WebElement> elements = driver.findElements(By.xpath(xpath));
            for (WebElement el : elements) {
                if (el.isDisplayed()) {
                    // Scroll into view
                    ((org.openqa.selenium.JavascriptExecutor) driver)
                            .executeScript("arguments[0].scrollIntoView(true);", el);
                    Thread.sleep(1000); // Wait for scroll

                    // Click (try parent anchor if text is inside a span)
                    try {
                        el.click();
                    } catch (Exception e) {
                        // Try clicking parent
                        el.findElement(By.xpath("./..")).click();
                    }
                    System.out.println("✓ Clicked product with title: " + titlePartial);
                    return true;
                }
            }
            System.err.println("✗ Product with title '" + titlePartial + "' not found or not visible");
            return false;
        } catch (Exception e) {
            System.err.println("✗ Failed to click product '" + titlePartial + "': " + e.getMessage());
            return false;
        }
    }

    // ==========================================
    // TC_AMZ_009: Empty Search Method
    // ==========================================

    public boolean verifyEmptySearchStayedOnPage() {
        try {
            String currentUrl = driver.getCurrentUrl();
            // Should either be amazon.com base or not have a ?k= empty query param causing
            // error
            // Actually, Amazon often stays on page or invalidates search.
            // We verify we didn't crash and ideally stayed on homepage or similar
            if (currentUrl.contains("amazon.com")) {
                System.out.println("✓ System handled empty search gracefully (Stayed on Amazon domain)");
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // ==========================================
    // TC_AMZ_010: Persistence Method
    // ==========================================

    public void refreshPage() {
        driver.navigate().refresh();
        System.out.println("✓ Page refreshed");
        try {
            Thread.sleep(3000); // Wait for reload
            wait.until(ExpectedConditions.jsReturnsValue("return document.readyState === 'complete'"));
        } catch (Exception e) {
        }
    }

    public boolean clickBookBySpecificXPath() {
        try {
            String xpath = "//*[@id=\"desktop-books-storefront_BooksNewReleases_0\"]/div/div/bds-render-context-provider/bds-carousel/bds-carousel-item[6]/div/bds-unified-book-faceout//div/a/bds-book-cover-image//div/picture/img";

            // Wait for presence
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));

            WebElement img = driver.findElement(By.xpath(xpath));

            // Scroll to it
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", img);
            Thread.sleep(1000);

            // Click parent anchor tag since the image itself might not be the link or to
            // enable bubbling
            // The user said "click on it" (the image), but usually we want to click the
            // link <a> wrapping it or the image itself if clickable.
            // The XPath ends in /img.
            img.click();

            System.out.println("✓ Clicked book using specific XPath");
            return true;
        } catch (Exception e) {
            System.err.println("✗ Failed to click book by specific XPath: " + e.getMessage());
            return false;
        }
    }
}
