# Amazon Automation Testing Guide

## Overview
This document provides comprehensive guidance on the Amazon Automation Test Suite, including test priorities, logging approach, screenshot capture, and how to interpret TestNG reports.

---

## Test Suite Organization

### Priority Levels

The test suite is organized into 4 priority levels based on criticality and test type:

#### **CRITICAL (Priority 1)** - Smoke Tests
These are the most important tests that validate core functionality. If these fail, the application is not usable.

- **TC_AMZ_001**: Verify Valid Product Search
  - Validates basic search functionality
  - Ensures products are displayed with required elements
  
- **TC_AMZ_005**: Add Single Item to Cart
  - Validates core e-commerce functionality
  - Ensures users can add items to cart

#### **HIGH (Priority 2)** - Core Functionality
Important functional tests that validate key features.

- **TC_AMZ_003**: Verify Search with Price Filter
  - Tests price filtering functionality
  - Validates filtered results are within range
  
- **TC_AMZ_004**: Verify Product Details Display
  - Tests product page navigation
  - Validates product details are displayed
  
- **TC_AMZ_006**: Update Cart Quantity
  - Tests cart quantity management
  - Validates cart updates correctly

#### **MEDIUM (Priority 3)** - Extended Functionality
Tests for additional features and workflows.

- **TC_AMZ_007**: Remove Item from Cart
  - Tests item removal functionality
  - Validates empty cart behavior
  
- **TC_AMZ_008**: Proceed to Checkout
  - Tests checkout flow initiation
  - Validates sign-in page redirection
  
- **TC_AMZ_010**: Cart Persistence After Refresh
  - Tests session management
  - Validates cart data persistence

#### **LOW (Priority 4)** - Negative Tests
Edge cases and error handling validation.

- **TC_AMZ_002**: Verify Search with Invalid Keyword
  - Tests handling of invalid search input
  - Validates error messages or suggestions
  
- **TC_AMZ_009**: Empty Search Validation
  - Tests empty search handling
  - Validates no application crash

---

## Logging Approach

### Log4j2 Framework
The project uses **Log4j2** for professional-grade logging with the following features:

- **Console Logging**: Real-time logs displayed during test execution
- **File Logging**: Persistent logs stored in `logs/automation.log`
- **Rolling Policy**: Log files rotate daily and when they reach 10MB
- **Log Levels**: INFO for general execution, DEBUG for troubleshooting, ERROR for failures

### Log File Location
```
Amazon-Automation/
├── logs/
│   ├── automation.log (current log file)
│   └── automation-2026-01-09-1.log (archived logs)
```

### Reading Logs
Each test execution logs:
1. **Test Start**: Test case ID and name
2. **Test Steps**: Detailed step-by-step execution
3. **Assertions**: Validation results
4. **Test End**: Final result (PASSED/FAILED)

Example log entry:
```
14:30:15.123 [main] INFO  testcase1 - ================================================================================
14:30:15.124 [main] INFO  testcase1 - # TEST CASE: TC_AMZ_001 - Verify Valid Product Search
14:30:15.125 [main] INFO  testcase1 - ================================================================================
14:30:15.126 [main] INFO  testcase1 - >>> Step 1 : Navigate to Amazon homepage
14:30:16.234 [main] INFO  testcase1 - Navigation successful to: https://www.amazon.com
```

---

## Screenshot Capture

### Automatic Screenshot on Failure
The `ScreenshotListener` automatically captures screenshots when tests fail.

**Screenshot Location:**
```
Amazon-Automation/
├── screenshots/
│   ├── testValidProductSearch_20260109_143045.png
│   └── testInvalidKeywordSearch_20260109_143120.png
```

**Naming Convention:**
```
{testMethodName}_{timestamp}.png
```

### Manual Screenshot Capture
You can also capture screenshots manually in test code:
```java
String screenshotPath = ScreenshotListener.captureScreenshot(driver, "debug_screenshot");
```

### Screenshot Features
- **Timestamp**: Unique timestamp prevents file overwrites
- **Auto-create Directory**: Screenshots folder created automatically
- **Full Page**: Captures entire browser window
- **PNG Format**: High-quality lossless format

---

## TestNG Reports

### HTML Report Location
After running tests, TestNG generates detailed HTML reports:

```
Amazon-Automation/
├── test-output/
│   ├── index.html (main report)
│   ├── emailable-report.html (summary report)
│   └── testng-results.xml (XML results)
```

### Viewing Reports
1. Navigate to `test-output/` folder
2. Open `index.html` in a web browser
3. View detailed test results, timings, and failure information

### Report Contents
- **Suite Summary**: Total tests, passed, failed, skipped
- **Test Details**: Individual test execution time and status
- **Failure Information**: Stack traces and error messages
- **Execution Timeline**: When each test ran

---

## Running Tests

### Run All Tests
```bash
mvn clean test
```

### Run Specific Priority
To run only CRITICAL tests (Priority 1):
```bash
# Edit testng.xml to include only priority 1 tests
mvn test
```

### Run Single Test Class
```bash
mvn test -Dtest=testcase1
```

### Run with Verbose Logging
```bash
mvn test -Dlog4j.configurationFile=src/test/resources/log4j2.xml
```

---

## Code Comments Guide

All test code includes comprehensive comments explaining:

### 1. **Waits**
- **Explicit Waits (WebDriverWait)**: Waits up to specified time for condition
  ```java
  // WebDriverWait is an explicit wait that waits up to 10 seconds
  // It's better than Thread.sleep() because it proceeds as soon as condition is met
  wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("element")));
  ```

- **Implicit Waits (Thread.sleep)**: Fixed duration waits
  ```java
  // Thread.sleep() waits for a fixed duration (3000ms = 3 seconds)
  // Used here because page needs time to reload after filter click
  Thread.sleep(3000);
  ```

### 2. **Loops**
- **For Loops**: Iteration and retry mechanisms
  ```java
  // Loop executes maximum 5 times (i = 0, 1, 2, 3, 4)
  for (int i = 0; i < 5; i++) {
      // Check condition
      if (condition) {
          break; // Exit loop immediately if condition met
      }
  }
  ```

### 3. **Conditional Logic**
- **If/Else**: Decision making
  ```java
  // Conditional block - only execute if cart is empty
  if (cartCount == 0) {
      // Verify empty cart message
  } else {
      // Cart has items
  }
  ```

### 4. **Logical Operators**
- **AND (&&)**: Both conditions must be true
  ```java
  // Logical AND - both conditions must be true
  if (url.contains("amazon.com") && url.contains("signin")) {
      // Both conditions satisfied
  }
  ```

- **OR (||)**: At least one condition must be true
  ```java
  // Logical OR - test passes if either condition is true
  if (noResultsMessage || hasSuggestions) {
      // At least one condition satisfied
  }
  ```

### 5. **Assertions**
- **assertTrue**: Condition must be true
- **assertFalse**: Condition must be false
- **assertEquals**: Values must be equal
- **assertNotNull**: Object must not be null

---

## Best Practices

### 1. **Run Tests in Order**
Tests are organized by priority. Run CRITICAL tests first to catch major issues early.

### 2. **Review Logs for Failures**
When a test fails:
1. Check the screenshot in `screenshots/` folder
2. Review the log file in `logs/automation.log`
3. Check TestNG report in `test-output/index.html`

### 3. **Clean Test Environment**
For consistent results:
```bash
mvn clean  # Clean previous build
mvn test   # Run fresh tests
```

### 4. **Update Test Data**
Test data is defined as constants at the top of each test class:
```java
private static final String SEARCH_KEYWORD = "laptop";
```

### 5. **Parallel Execution**
Currently disabled for stability. To enable, update `testng.xml`:
```xml
<suite name="..." parallel="methods" thread-count="3">
```

---

## Troubleshooting

### Issue: Tests Fail Due to Popups
**Solution**: The framework handles popups automatically in `AmazonSearchFunctions.handleAddressPopUp()`

### Issue: Screenshots Not Captured
**Solution**: Ensure `ScreenshotListener` is registered in `testng.xml`

### Issue: Logs Not Generated
**Solution**: Verify `log4j2.xml` exists in `src/test/resources/`

### Issue: ChromeDriver Version Mismatch
**Solution**: WebDriverManager automatically downloads correct version. Ensure internet connection.

---

## Contact & Support

For questions or issues:
1. Review this guide
2. Check logs and screenshots
3. Review TestNG reports
4. Check code comments for detailed explanations

---

**Version**: 2.0  
**Last Updated**: January 2026  
**Author**: Senior Automation Tester
