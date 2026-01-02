package utils;

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

public class ScreenshotListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println("xxxxx Method " + result.getName() + " failed xxxxx");
        System.out.println("Taking screenshot...");

        // 1. Get the test class instance
        Object currentClass = result.getInstance();
        WebDriver driver = null;

        // 2. Access the 'driver' field via reflection
        try {
            // Assuming the field name is always "driver" as seen in testcase1.java
            java.lang.reflect.Field driverField = currentClass.getClass().getDeclaredField("driver");
            driverField.setAccessible(true);
            driver = (WebDriver) driverField.get(currentClass);
        } catch (Exception e) {
            System.err.println("Could not retrieve WebDriver via reflection: " + e.getMessage());
        }

        // 3. Take screenshot if driver is found
        if (driver != null) {
            captureScreenshot(driver, result.getName());
        } else {
            System.err.println("Driver is null, cannot take screenshot.");
        }
    }

    public static String captureScreenshot(WebDriver driver, String validMethodName) {
        File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

        // Generate timestamp
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = validMethodName + "_" + timestamp + ".png";

        // Define screenshot directory
        String folderPath = System.getProperty("user.dir") + File.separator + "screenshots";

        try {
            // Create directory if not exists
            Path path = Paths.get(folderPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }

            // Save file
            File destFile = new File(folderPath + File.separator + fileName);
            Files.copy(srcFile.toPath(), destFile.toPath());

            System.out.println("Screenshot saved successfully at: " + destFile.getAbsolutePath());
            System.out
                    .println("Comment: Test case '" + validMethodName + "' failed. Screenshot captured for analysis.");
            return destFile.getAbsolutePath();

        } catch (IOException e) {
            System.err.println("Failed to save screenshot: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
    }

    @Override
    public void onTestSuccess(ITestResult result) {
    }

    @Override
    public void onTestSkipped(ITestResult result) {
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    }

    @Override
    public void onStart(ITestContext context) {
    }

    @Override
    public void onFinish(ITestContext context) {
    }
}
