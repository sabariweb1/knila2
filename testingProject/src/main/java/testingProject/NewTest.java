package testingProject;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger; 
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

public class NewTest {

    private WebDriver driver;
    private Logger logger = LogManager.getLogger(NewTest.class);

    @BeforeClass
    public void setup() {
    	
    	ChromeOptions options = new ChromeOptions();
    	options.addArguments("--remote-allow-origins=*");
    	ChromeDriver driver = new ChromeDriver(options);
        WebDriverManager.chromedriver().setup();
 //       System.setProperty("webdriver.chrome.driver","C:\\Users\\Sabari\\Downloads\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe");
        WebDriver driver1 = new ChromeDriver();
        driver1.manage().window().maximize();
        driver1.get("https://demo.openmrs.org/openmrs/login.htm");
        driver1.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @AfterMethod
    public void captureScreenshotAndLogResult(ITestResult result) {
        if (ITestResult.FAILURE == result.getStatus()) {
            logger.error("Test Failed: " + result.getName());
            captureScreenshot(result.getName());
        } else {
            logger.info("Test Passed: " + result.getName());
            captureScreenshot(result.getName());
        }
    }

    public void captureScreenshot(String testName) {
        TakesScreenshot ts = (TakesScreenshot) driver;
        File source = ts.getScreenshotAs(OutputType.FILE);
        try {
            Files.copy(source.toPath(), Paths.get("screenshots", testName + ".png"));
            logger.info("Screenshot captured for test case: " + testName);
        } catch (IOException e) {
            logger.error("Failed to capture screenshot: " + e.getMessage());
        }
    }

    @Test(description = "1. Login to OpenMRS and verify dashboard redirection")
    public void loginAndVerifyDashboard() {
        logger.info("Navigating to OpenMRS login page");
        driver.get("https://demo.openmrs.org/openmrs/login.htm");

        logger.info("Entering username and password");
        driver.findElement(By.id("username")).sendKeys("Admin");
        driver.findElement(By.id("password")).sendKeys("Admin123");

        logger.info("Selecting location and logging in");
        WebElement location = driver.findElement(By.id("sessionLocation"));
        location.click();
        location.findElement(By.xpath("//li[text()='Inpatient Ward']")).click();
        driver.findElement(By.id("loginButton")).click();

        logger.info("Verifying redirection to dashboard");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement dashboardLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("referenceapplication-registrationapp-registerPatient-homepageLink-referenceapplication-registrationapp-registerPatient-homepageLink-extension")));
        Assert.assertTrue(dashboardLink.isDisplayed(), "Dashboard redirection failed");
    }

    @Test(description = "2. Register a new patient")
    public void registerNewPatient() {
        loginAndVerifyDashboard();

        logger.info("Clicking on Register a Patient menu");
        driver.findElement(By.id("referenceapplication-registrationapp-registerPatient-homepageLink-referenceapplication-registrationapp-registerPatient-homepageLink-extension")).click();

        logger.info("Filling in patient demographics and contact info");
        driver.findElement(By.name("givenName")).sendKeys("John");
        driver.findElement(By.name("familyName")).sendKeys("Doe");
        driver.findElement(By.id("gender-M")).click();
        driver.findElement(By.name("birthdateDay")).sendKeys("25");
        driver.findElement(By.name("birthdateMonth")).sendKeys("01");
        driver.findElement(By.name("birthdateYear")).sendKeys("1990");
        driver.findElement(By.name("address1")).sendKeys("1234 Elm Street");
        driver.findElement(By.name("phoneNumber")).sendKeys("1234567890");

        logger.info("Clicking on 'Next' and verifying the confirmation page");
        driver.findElement(By.id("next-button")).click();

        logger.info("Verifying entered details on the confirmation page");
        Assert.assertEquals(driver.findElement(By.xpath("//span[@data-bind='text: givenName']")).getText(), "John");
        Assert.assertEquals(driver.findElement(By.xpath("//span[@data-bind='text: familyName']")).getText(), "Doe");
        Assert.assertEquals(driver.findElement(By.xpath("//span[@data-bind='text: gender']")).getText(), "Male");
        Assert.assertEquals(driver.findElement(By.xpath("//span[@data-bind='text: birthdate']")).getText(), "25/01/1990");
        Assert.assertEquals(driver.findElement(By.xpath("//span[@data-bind='text: address1']")).getText(), "1234 Elm Street");
        Assert.assertEquals(driver.findElement(By.xpath("//span[@data-bind='text: phoneNumber']")).getText(), "1234567890");

        logger.info("Confirming registration and verifying patient details page redirection");
        driver.findElement(By.id("submit")).click();
        Assert.assertTrue(driver.getPageSource().contains("Patient Details"), "Patient Details page not displayed");
    }

    @Test(description = "3. Start a visit and upload attachment")
    public void startVisitAndUploadAttachment() {
        registerNewPatient();

        logger.info("Starting a visit for the newly registered patient");
        driver.findElement(By.id("org-openmrs-module-coreapps-activeVisitsHomepageLink-org-openmrs-module-coreapps-activeVisitsHomepageLink-extension")).click();
        driver.findElement(By.id("start-visit-with-no-date")).click();

        logger.info("Clicking on 'Attachments' and uploading a file");
        driver.findElement(By.id("attachments.attachments.visitActions.default")).click();
        driver.findElement(By.id("visit-doc")).sendKeys(new File("path/to/attachment.jpg").getAbsolutePath());
        driver.findElement(By.id("submit-attachment")).click();

        logger.info("Verifying success toaster message for attachment");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement toasterMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("toast-message")));
        Assert.assertTrue(toasterMessage.isDisplayed(), "Attachment success message not displayed");

        logger.info("Verifying attachment in patient details");
        driver.findElement(By.id("referenceapplication-vitals-referenceapplication-vitals-homepageLink-referenceapplication-vitals-homepageLink-extension")).click();
        Assert.assertTrue(driver.getPageSource().contains("Attachment"), "Attachment not found in the patient details");
    }

    @Test(description = "4. Verify BMI calculation")
    public void verifyBMICalculation() {
        startVisitAndUploadAttachment();

        logger.info("Clicking on 'Capture Vitals' and entering height and weight");
        driver.findElement(By.id("vitals.vitals-actions.default")).click();
        driver.findElement(By.name("height")).sendKeys("175");
        driver.findElement(By.name("weight")).sendKeys("70");

        logger.info("Verifying BMI calculation");
        double heightInMeters = 175 / 100.0;
        double expectedBMI = 70 / (heightInMeters * heightInMeters);
        WebElement bmiElement = driver.findElement(By.id("bmi-calculated-value"));
        double actualBMI = Double.parseDouble(bmiElement.getText());
        Assert.assertEquals(actualBMI, expectedBMI, "BMI calculation is incorrect");

        logger.info("Saving the form");
        driver.findElement(By.id("save-form")).click();
        Assert.assertTrue(driver.getPageSource().contains("Vitals saved"), "Vitals were not saved");
    }

    @Test(description = "5. Merge visits and verify")
    public void mergeVisits() {
        verifyBMICalculation();

        logger.info("Clicking on 'Merge Visits' and selecting the two visits");
        driver.findElement(By.id("merge-visits")).click();
        driver.findElement(By.xpath("//input[@type='checkbox'][1]")).click(); // select the first visit
        driver.findElement(By.xpath("//input[@type='checkbox'][2]")).click(); // select the second visit
        driver.findElement(By.id("merge-selected-visits")).click();

        logger.info("Verifying that the two visits were merged into one");
        driver.findElement(By.id("return-to-patient-details")).click();
        Assert.assertTrue(driver.getPageSource().contains("1 visit"), "Visits were not merged correctly");
    }

    @Test(description = "6. Delete a patient")
    public void deletePatient() {
        mergeVisits();

        logger.info("Clicking on 'Delete Patient' and providing a reason");
        driver.findElement(By.id("delete-patient")).click();
        driver.findElement(By.id("delete-reason")).sendKeys("Test completed");

        logger.info("Confirming deletion and verifying patient is no longer in the system");
        driver.findElement(By.id("confirm-delete")).click();
        Assert.assertTrue(driver.getPageSource().contains("No matching records found"), "Deleted patient is still listed");
    }
}
