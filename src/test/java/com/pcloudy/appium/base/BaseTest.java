package com.pcloudy.appium.base;

import com.pcloudy.appium.data.TestData;
import com.pcloudy.appium.driver.DriverFactory;
import com.pcloudy.appium.pages.BookFlightPage;
import com.pcloudy.appium.pages.HomePage;
import com.pcloudy.appium.pages.LoginPage;
import com.pcloudy.appium.pages.PassengerDetailsPage;
import com.pcloudy.appium.pages.PaymentPage;
import com.pcloudy.appium.utils.ScreenshotUtil;

import io.appium.java_client.android.AndroidDriver;

import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.IOException;

/** Opens a fresh app session for every test and captures a screenshot when a test fails. */
public abstract class BaseTest {

    protected AndroidDriver driver;

    @BeforeMethod(alwaysRun = true)
    public void startSession() {
        driver = DriverFactory.createDriver();
    }

    @AfterMethod(alwaysRun = true)
    public void endSession(ITestResult result) {
        if (driver == null) {
            return;
        }
        try {
            // Final screen of every test; failures are prefixed with the test name.
            saveScreenshot(result.getStatus() == ITestResult.FAILURE ? result.getMethod().getMethodName() : "");
        } finally {
            driver.quit();
            driver = null;
        }
    }

    // ---- Shared journeys so each test only spells out the steps it verifies ----

    protected HomePage loginAndAcceptLicence() {
        return new LoginPage(driver)
                .loginAs(TestData.USERNAME, TestData.PASSWORD)
                .accept();
    }

    protected BookFlightPage openBookFlight() {
        return loginAndAcceptLicence().openBookFlight();
    }

    /** Searches a one-way Economy trip with 1 adult and selects the first flight. */
    protected PassengerDetailsPage selectFirstOneWayFlight(String from, String to) {
        return openBookFlight()
                .selectFrom(from)
                .selectTo(to)
                .chooseOneWay()
                .setDepartDate(TestData.departDate())
                .searchFlights()
                .selectFlight(0);
    }

    protected PaymentPage reachPayment() {
        return selectFirstOneWayFlight(TestData.MUMBAI, TestData.DUBAI)
                .enterPassenger(TestData.GIVEN_NAME, TestData.LAST_NAME, TestData.EMAIL, TestData.PHONE)
                .continueToPayment();
    }

    protected void captureScreenShots() throws IOException {
        ScreenshotUtil.captureScreenShots(driver);
    }

    private void saveScreenshot(String testName) {
        try {
            ScreenshotUtil.captureScreenShots(driver, testName);
        } catch (IOException | RuntimeException e) {
            Reporter.log("Could not save screenshot: " + e.getMessage(), true);
        }
    }
}
