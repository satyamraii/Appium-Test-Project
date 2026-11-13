package com.pcloudy.appium.pages;

import io.appium.java_client.android.AndroidDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class OtpPage extends BasePage {

    private static final By HEADING = id("tv_otp_heading");
    private static final By MESSAGE = id("tv_otp_message");
    private static final By OTP_VALUE = id("tv_otp_value");
    private static final By OTP_INPUT = id("et_otp");
    private static final By VERIFY = id("btn_verify_otp");
    private static final By RESEND = id("btn_resend_otp");
    private static final By ERROR = id("tv_otp_error");

    public OtpPage(AndroidDriver driver) {
        super(driver);
        waitForVisible(OTP_VALUE);
        captureScreen();
    }

    public String getHeading() {
        return textOf(HEADING);
    }

    public String getMessage() {
        return textOf(MESSAGE);
    }

    /** The demo OTP shown on screen in place of an SMS. */
    public String getOtp() {
        return textOf(OTP_VALUE);
    }

    public OtpPage enterOtp(String otp) {
        type(OTP_INPUT, otp);
        return this;
    }

    /** Verifies the OTP and waits out the "Processing payment…" step. */
    public BookingConfirmationPage verify() {
        tap(VERIFY);
        return new BookingConfirmationPage(driver);
    }

    public String verifyExpectingError() {
        tap(VERIFY);
        return textOf(ERROR);
    }

    public OtpPage resendOtp() {
        String previous = getOtp();
        tap(RESEND);
        wait.until(ExpectedConditions.not(ExpectedConditions.textToBe(OTP_VALUE, previous)));
        return this;
    }

    public boolean isDisplayed() {
        return isDisplayedNow(OTP_VALUE);
    }
}
