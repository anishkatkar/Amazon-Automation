# Amazon Automation Test Suite

## TC_AMZ_001: Verify Valid Product Search

**Priority:** HIGH  
**Type:** Smoke Test  
**Automation:** YES

### Test Objective
Validate that searching with a valid keyword returns relevant product results on Amazon.

---

## Project Structure

```
Amazon-Automation/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── AmazonSearchFunctions.java    # Reusable functions for search tests
│   └── test/
│       ├── java/
│       │   └── testcase1.java                # TC_AMZ_001 test case
│       └── resources/
│           └── testng.xml                    # TestNG configuration
├── pom.xml                                    # Maven dependencies
└── README.md                                  # This file
```

---

## Prerequisites

1. **Java JDK 17 or higher** installed
2. **Maven** installed
3. **Chrome browser** installed
4. **Internet connection** (for accessing Amazon and downloading WebDriver)

---

## Dependencies

The project uses the following dependencies (already configured in `pom.xml`):

- **Selenium WebDriver 4.16.1** - Browser automation
- **TestNG 7.8.0** - Test framework
- **WebDriverManager 5.6.3** - Automatic driver management

---

## Setup Instructions

### 1. Install Dependencies

Navigate to the project directory and run:

```bash
cd /Users/anishk/IdeaProjects/Amazon-Automation
mvn clean install
```

This will download all required dependencies.

### 2. Verify Setup

Check that dependencies are installed:

```bash
mvn dependency:tree
```

---

## Running the Tests

### Option 1: Run via Maven (Recommended)

```bash
mvn test
```

### Option 2: Run via TestNG XML

```bash
mvn test -DsuiteXmlFile=src/test/resources/testng.xml
```

### Option 3: Run specific test class

```bash
mvn test -Dtest=testcase1
```

### Option 4: Run from IDE

1. Open the project in IntelliJ IDEA or Eclipse
2. Right-click on `testcase1.java`
3. Select "Run 'testcase1'" or "Run as TestNG Test"

---

## Test Details

### Test Data
- **Search Keyword:** "laptop"
- **Expected Minimum Results:** 50 products
- **Amazon URL:** https://www.amazon.com

### Test Steps (12 Steps)

| Step | Action | Expected Result |
|------|--------|----------------|
| 1 | Navigate to Amazon homepage | Homepage loads successfully |
| 2 | Locate search box | Search box is visible and enabled |
| 3 | Clear existing text | Search box is empty |
| 4 | Enter search keyword "laptop" | Text appears in search box |
| 5 | Locate search button | Search button is visible |
| 6 | Click search button | Search results page loads |
| 7 | Wait for results to load | Results appear on page |
| 8 | Verify results count text | Text shows "X results for 'laptop'" |
| 9 | Count number of product cards | Count ≥ 50 products |
| 10 | Verify first product has image | Image is displayed |
| 11 | Verify first product has title | Title text is present |
| 12 | Verify first product has price | Price is displayed |

### Expected Final Result
✓ Search results page displays  
✓ Minimum 50 products shown  
✓ Each product has: image, title, price  
✓ Results are relevant to "laptop"

---

## Reusable Functions

The `AmazonSearchFunctions.java` class contains all reusable functions:

### Core Functions
- `navigateToAmazonHomepage(String url)` - Navigate to Amazon
- `locateSearchBox()` - Find and return search box element
- `clearSearchBox(WebElement searchBox)` - Clear search box
- `enterSearchKeyword(WebElement searchBox, String keyword)` - Enter search text
- `locateSearchButton()` - Find and return search button
- `clickSearchButton(WebElement searchButton)` - Click search
- `waitForSearchResults()` - Wait for results to load
- `verifyResultsCountText(String keyword)` - Verify results text
- `countProductCards()` - Count total products
- `verifyMinimumProductCount(int actual, int expected)` - Validate count

### Validation Functions
- `verifyFirstProductHasImage()` - Check first product image
- `verifyFirstProductHasTitle()` - Check first product title
- `verifyFirstProductHasPrice()` - Check first product price
- `verifyAllProductsHaveRequiredElements(int maxToCheck)` - Validate multiple products

### Utility Functions
- `getCurrentUrl()` - Get current page URL
- `getPageTitle()` - Get current page title

---

## Element Locators

All locators are centralized in `AmazonSearchFunctions.java`:

```java
SEARCH_BOX_ID = "twotabsearchtextbox"
SEARCH_BUTTON_ID = "nav-search-submit-button"
SEARCH_RESULT_CSS = "[data-component-type='s-search-result']"
PRODUCT_IMAGE_CSS = ".s-image"
PRODUCT_TITLE_CSS = ".a-size-medium.a-color-base.a-text-normal"
PRODUCT_PRICE_CSS = ".a-price-whole"
```

---

## Test Output

The test provides detailed console output:

```
================================================================================
TC_AMZ_001: Verify Valid Product Search
Priority: HIGH | Type: Smoke Test | Automation: YES
================================================================================

✓ Step 1: Successfully navigated to Amazon homepage
✓ Step 2: Search box located and is enabled
✓ Step 3: Search box cleared successfully
✓ Step 4: Successfully entered keyword: 'laptop'
✓ Step 5: Search button located successfully
✓ Step 6: Search button clicked successfully
✓ Step 7: Search results loaded successfully
✓ Step 8: Results count text verified
✓ Step 9: Found 60 product cards
✓ Step 10: First product has a displayed image
✓ Step 11: First product has title
✓ Step 12: First product has price

================================================================================
TEST RESULT: PASSED ✓
================================================================================
```

---

## Troubleshooting

### Issue: ChromeDriver not found
**Solution:** WebDriverManager should handle this automatically. If issues persist:
```bash
mvn clean install -U
```

### Issue: Test fails due to timeout
**Solution:** Increase wait time in `AmazonSearchFunctions.java`:
```java
this.wait = new WebDriverWait(driver, Duration.ofSeconds(20)); // Increase from 10 to 20
```

### Issue: Elements not found
**Solution:** Amazon's UI may have changed. Update locators in `AmazonSearchFunctions.java`

### Issue: Region-specific Amazon site
**Solution:** Amazon may redirect based on location. Update URL in `testcase1.java`:
```java
private static final String AMAZON_URL = "https://www.amazon.in"; // For India
```

---

## Extending the Tests

### Adding New Test Cases

1. Create a new test method in `testcase1.java`:
```java
@Test(priority = 3)
public void testNewScenario() {
    // Use existing functions from AmazonSearchFunctions
}
```

2. Or create a new test class and reuse `AmazonSearchFunctions`

### Adding New Functions

Add new reusable functions to `AmazonSearchFunctions.java`:
```java
public boolean newFunction() {
    // Implementation
}
```

---

## Best Practices Implemented

✓ **Page Object Pattern** - Functions separated from test logic  
✓ **Explicit Waits** - 10-second timeout for dynamic elements  
✓ **Centralized Locators** - Easy maintenance  
✓ **Detailed Logging** - Step-by-step console output  
✓ **Error Handling** - Try-catch blocks with meaningful messages  
✓ **Assertions** - TestNG assertions for validation  
✓ **Reusability** - Functions can be used across multiple tests  
✓ **Clean Code** - Well-documented and organized  

---

## Contact & Support

For issues or questions about this test automation framework, please refer to the test case documentation or contact the QA team.

---

**Last Updated:** December 24, 2025  
**Version:** 1.0  
**Author:** Automation Team
