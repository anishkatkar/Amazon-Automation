package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * TestLogger - Centralized logging utility for Amazon Automation Test Suite
 * 
 * This class provides a wrapper around Log4j2 for consistent logging across all
 * test cases.
 * It offers convenience methods for different log levels and formatted test
 * step logging.
 * 
 * Usage Example:
 * 
 * <pre>
 * Logger logger = TestLogger.getLogger(TestClassName.class);
 * logger.info("Test started");
 * TestLogger.logTestStep("Step 1", "Navigate to homepage");
 * </pre>
 * 
 * @author Senior Automation Tester
 * @version 1.0
 */
public class TestLogger {

    /**
     * Get a logger instance for a specific class
     * 
     * @param clazz The class for which to create the logger
     * @return Logger instance configured for the class
     */
    public static Logger getLogger(Class<?> clazz) {
        return LogManager.getLogger(clazz);
    }

    /**
     * Log a test step with formatted output
     * This method creates a visually distinct log entry for test steps
     * 
     * @param stepNumber  The step number (e.g., "Step 1", "Step 2")
     * @param description Description of the step
     */
    public static void logTestStep(String stepNumber, String description) {
        Logger logger = LogManager.getLogger("TestSteps");
        logger.info(">>> {} : {}", stepNumber, description);
    }

    /**
     * Log a test step with status (PASS/FAIL)
     * 
     * @param stepNumber  The step number
     * @param description Description of the step
     * @param status      Status of the step (PASS/FAIL)
     */
    public static void logTestStepWithStatus(String stepNumber, String description, String status) {
        Logger logger = LogManager.getLogger("TestSteps");
        String symbol = status.equalsIgnoreCase("PASS") ? "✓" : "✗";
        logger.info(">>> {} : {} - {} {}", stepNumber, description, symbol, status);
    }

    /**
     * Log a section header for better readability in logs
     * 
     * @param sectionName Name of the section
     */
    public static void logSection(String sectionName) {
        Logger logger = LogManager.getLogger("TestSteps");
        logger.info("================================================================================");
        logger.info("  {}", sectionName);
        logger.info("================================================================================");
    }

    /**
     * Log test case start
     * 
     * @param testCaseId   Test case ID (e.g., "TC_AMZ_001")
     * @param testCaseName Test case name
     */
    public static void logTestStart(String testCaseId, String testCaseName) {
        Logger logger = LogManager.getLogger("TestExecution");
        logger.info("");
        logger.info("################################################################################");
        logger.info("# TEST CASE: {} - {}", testCaseId, testCaseName);
        logger.info("################################################################################");
    }

    /**
     * Log test case end with result
     * 
     * @param testCaseId Test case ID
     * @param result     Test result (PASSED/FAILED/SKIPPED)
     */
    public static void logTestEnd(String testCaseId, String result) {
        Logger logger = LogManager.getLogger("TestExecution");
        logger.info("################################################################################");
        logger.info("# TEST CASE {} - RESULT: {}", testCaseId, result);
        logger.info("################################################################################");
        logger.info("");
    }
}
