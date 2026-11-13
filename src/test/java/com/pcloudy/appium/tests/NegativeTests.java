package com.pcloudy.appium.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.pcloudy.appium.base.BaseTest;
import com.pcloudy.appium.data.TestData;
import com.pcloudy.appium.pages.LicensePage;
import com.pcloudy.appium.pages.LoginPage;
import com.pcloudy.appium.pages.OtpPage;
import com.pcloudy.appium.pages.PaymentPage;

import org.testng.annotations.Test;

public class NegativeTests extends BaseTest {

    @Test(priority = 1, description = "NTC_01: Wrong password is rejected on the Login screen")
    public void wrongPasswordIsRejected() {
        LoginPage login = new LoginPage(driver).loginExpectingError(TestData.USERNAME, TestData.WRONG_PASSWORD);

        assertEquals(login.getErrorMessage(), "Invalid user name or password");
        assertTrue(login.isDisplayed(), "User should stay on the Login screen");
    }

    @Test(priority = 2, description = "NTC_02: Refusing the licence shows a toast and blocks access to Home")
    public void refusingLicenceBlocksAccess() {
        LicensePage licence = new LoginPage(driver).loginAs(TestData.USERNAME, TestData.PASSWORD);

        String toast = licence.refuseAndReadToast();

        assertEquals(toast, "It must accept the T&C for Book Ticket");
        assertTrue(licence.isDisplayed(), "User should stay on the Licence screen");
    }

    @Test(priority = 3, description = "NTC_03: Searching with the same From and To city shows an error")
    public void sameFromAndToCityIsRejected() {
        String error = openBookFlight()
                .selectFrom(TestData.BANGALORE)
                .selectTo(TestData.BANGALORE)
                .chooseOneWay()
                .setDepartDate(TestData.departDate())
                .searchExpectingError();

        assertEquals(error, "Departure and destination cities cannot be the same");
    }

    @Test(priority = 4, description = "NTC_04: An expired card is rejected on the Payment screen")
    public void expiredCardIsRejected() {
        PaymentPage payment = reachPayment()
                .selectBank(TestData.BANK_HDFC)
                .selectCardType(TestData.CREDIT_CARD)
                .enterCard(TestData.CARD_NUMBER, TestData.EXPIRED_CARD_EXPIRY, TestData.CVV);

        assertEquals(payment.payExpectingError(), "Card has expired");
    }

    @Test(priority = 5, description = "NTC_05: A wrong OTP is rejected and the booking is not confirmed")
    public void wrongOtpIsRejected() {
        OtpPage otp = reachPayment()
                .selectBank(TestData.BANK_HDFC)
                .selectCardType(TestData.DEBIT_CARD)
                .enterCard(TestData.CARD_NUMBER, TestData.validCardExpiry(), TestData.CVV)
                .pay();
        String wrongOtp = otp.getOtp().equals("000000") ? "111111" : "000000";

        String error = otp.enterOtp(wrongOtp).verifyExpectingError();

        assertEquals(error, "Invalid OTP. Please try again");
        assertTrue(otp.isDisplayed(), "User should stay on the OTP screen");
    }
}
