package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ScreenshotListener - TestNG Listener for automatic screenshot capture
 * 
 * This class implements ITestListener interface to automatically capture
 * screenshots
 * when test cases fail. It uses Java Reflection to access the WebDriver
 * instance
 * from test classes.
 * 
 * Key Features:
 * - Automatic screenshot capture on test failure
 * - Timestamp-based file naming for uniqueness
 * - Organized storage in screenshots/ folder
 * - Detailed logging of screenshot operations
 * 
 * How it works:
 * 1. TestNG calls onTestFailure() when a test fails
 * 2. Reflection is used to access the 'driver' field from test class
 * 3. Screenshot is captured and saved with timestamp
 * 4. File path is logged for easy access
 * 
 * @author Senior Automation Tester
 * @version 2.0
 */
public class ScreenshotListener implements ITestListener {

    // Logger instance for this class
    private static final Logger logger = LogManager.getLogger(ScreenshotListener.class);

    /**
     * Called when a test fails
     * This method is automatically invoked by TestNG framework
     * 
     * @param result ITestResult object containing test execution details
     */
    @Override
    public void onTestFailure(ITestResult result) {
        // Log the failure with test method name
        logger.error("xxxxx Method {} failed xxxxx", result.getName());
        logger.info("Initiating screenshot capture...");

        // Step 1: Get the test class instance from ITestResult
        // The getInstance() method returns the object of the test class that was
        // executed
        Object currentClass = result.getInstance();
        WebDriver driver = null;

        // Step 2: Use Java Reflection to access the 'driver' field
        // Reflection allows us to access private fields at runtime
        try {
            // getDeclaredField() retrieves the field named "driver" from the test class
            // This assumes all test classes have a field named "driver"
            java.lang.reflect.Field driverField = currentClass.getClass().getDeclaredField("driver");

            // setAccessible(true) allows us to access private fields
            // Without this, we would get IllegalAccessException for private fields
            driverField.setAccessible(true);

            // Get the actual WebDriver object from the field
            // We cast it to WebDriver type since the field returns Object
            driver = (WebDriver) driverField.get(currentClass);

            logger.debug("Successfully retrieved WebDriver instance via reflection");
        } catch (NoSuchFieldException e) {
            // This exception occurs if the test class doesn't have a field named "driver"
            logger.error("Could not find 'driver' field in test class: {}", e.getMessage());
        } catch (IllegalAccessException e) {
            // This exception occurs if we can't access the field (shouldn't happen after
            // setAccessible)
            logger.error("Could not access 'driver' field: {}", e.getMessage());
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            logger.error("Unexpected error during reflection: {}", e.getMessage());
        }

        // Step 3: Capture screenshot if driver was successfully retrieved
        if (driver != null) {
            String screenshotPath = captureScreenshot(driver, result.getName());
            if (screenshotPath != null) {
                logger.info("Screenshot captured successfully: {}", screenshotPath);
            }
        } else {
            logger.error("Driver is null, cannot take screenshot.");
        }
    }

    /**
     * Captures screenshot and saves it to file
     * This is a static method so it can be called from test classes directly
     * 
     * @param driver         WebDriver instance to capture screenshot from
     * @param testMethodName Name of the test method (used in filename)
     * @return Absolute path of saved screenshot file, or null if failed
     */
    public static String captureScreenshot(WebDriver driver, String testMethodName) {
        try {
            // Step 1: Cast WebDriver to TakesScreenshot interface
            // TakesScreenshot is a Selenium interface that provides screenshot capability
            // getScreenshotAs() captures the current browser window as a file
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            logger.debug("Screenshot captured in memory");

            // Step 2: Generate timestamp for unique filename
            // Format: yyyyMMdd_HHmmss (e.g., 20260109_143045)
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

            // Create filename: testMethodName_timestamp.png
            String fileName = testMethodName + "_" + timestamp + ".png";

            // Step 3: Define screenshot directory path
            // System.getProperty("user.dir") returns the project root directory
            // File.separator is OS-independent path separator (/ on Unix, \ on Windows)
            String folderPath = System.getProperty("user.dir") + File.separator + "screenshots";

            // Step 4: Create directory if it doesn't exist
            Path path = Paths.get(folderPath);
            if (!Files.exists(path)) {
                // createDirectories() creates the folder and any necessary parent folders
                Files.createDirectories(path);
                logger.info("Created screenshots directory: {}", folderPath);
            }

            // Step 5: Copy screenshot file to destination
            File destFile = new File(folderPath + File.separator + fileName);

            // Files.copy() copies the temporary screenshot to our desired location
            Files.copy(srcFile.toPath(), destFile.toPath());

            // Step 6: Log success with file path
            logger.info("Screenshot saved: {}", destFile.getAbsolutePath());
            logger.info("Test case '{}' failed. Screenshot captured for analysis.", testMethodName);

            // Return the absolute path for reference
            return destFile.getAbsolutePath();

        } catch (IOException e) {
            // IOException can occur during file operations (create directory, copy file)
            logger.error("Failed to save screenshot: {}", e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            logger.error("Unexpected error during screenshot capture: {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Called when a test starts
     * Currently not implemented, but available for future enhancements
     */
    @Override
    public void onTestStart(ITestResult result) {
        logger.info("Starting test: {}", result.getName());
    }

    /**
     * Called when a test passes
     * Currently not implemented, but could be used to capture success screenshots
     */
    @Override
    public void onTestSuccess(ITestResult result) {
        logger.info("Test passed: {}", result.getName());
    }

    /**
     * Called when a test is skipped
     */
    @Override
    public void onTestSkipped(ITestResult result) {
        logger.warn("Test skipped: {}", result.getName());
    }

    /**
     * Called when a test fails but is within success percentage
     * Rarely used, but part of ITestListener interface
     */
    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        logger.warn("Test failed but within success percentage: {}", result.getName());
    }

    /**
     * Called before any test methods are invoked
     * Can be used for suite-level setup
     */
    @Override
    public void onStart(ITestContext context) {
        logger.info("========================================");
        logger.info("Test Suite Started: {}", context.getName());
        logger.info("========================================");
    }

    /**
     * Called after all test methods have been invoked
     * Can be used for suite-level cleanup and reporting
     */
    @Override
    public void onFinish(ITestContext context) {
        logger.info("========================================");
        logger.info("Test Suite Finished: {}", context.getName());
        logger.info("Total Tests: {}", context.getAllTestMethods().length);
        logger.info("Passed: {}", context.getPassedTests().size());
        logger.info("Failed: {}", context.getFailedTests().size());
        logger.info("Skipped: {}", context.getSkippedTests().size());
        logger.info("========================================");
    }
}
