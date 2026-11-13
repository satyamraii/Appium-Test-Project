package com.pcloudy.appium.pages;

import io.appium.java_client.android.AndroidDriver;

import org.openqa.selenium.By;

public class FindBookingPage extends BasePage {

    private static final By REFERENCE = id("et_booking_reference");
    private static final By GIVEN_NAME = id("et_find_given_name");
    private static final By LAST_NAME = id("et_find_last_name");
    private static final By FIND = id("btn_find_booking");
    private static final By ERROR = id("tv_find_booking_error");

    public FindBookingPage(AndroidDriver driver) {
        super(driver);
        waitForVisible(REFERENCE);
        captureScreen();
    }

    public FindBookingPage enterDetails(String reference, String givenName, String lastName) {
        type(REFERENCE, reference);
        type(GIVEN_NAME, givenName);
        type(LAST_NAME, lastName);
        return this;
    }

    public BookingConfirmationPage findBooking() {
        tap(FIND);
        return new BookingConfirmationPage(driver);
    }

    public String findBookingExpectingError() {
        tap(FIND);
        return textOf(ERROR);
    }
}
