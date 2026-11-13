package com.pcloudy.appium.pages;

import io.appium.java_client.android.AndroidDriver;

import org.openqa.selenium.By;

public class HomePage extends BasePage {

    private static final By TITLE = id("tv_home_title");
    private static final By WELCOME = id("tv_welcome");
    private static final By SINGLE_BOOKING_REFERENCE = id("btn_single_booking_reference");
    private static final By BOOK_FLIGHT = id("btn_book_flight");
    private static final By LOGOUT = id("btn_logout");

    public HomePage(AndroidDriver driver) {
        super(driver);
        waitForVisible(BOOK_FLIGHT);
        captureScreen();
    }

    public String getTitle() {
        return textOf(TITLE);
    }

    public String getWelcomeText() {
        return textOf(WELCOME);
    }

    public boolean isBookFlightDisplayed() {
        return isDisplayedNow(BOOK_FLIGHT);
    }

    public boolean isSingleBookingReferenceDisplayed() {
        return isDisplayedNow(SINGLE_BOOKING_REFERENCE);
    }

    public BookFlightPage openBookFlight() {
        tap(BOOK_FLIGHT);
        return new BookFlightPage(driver);
    }

    public FindBookingPage openFindBooking() {
        tap(SINGLE_BOOKING_REFERENCE);
        return new FindBookingPage(driver);
    }

    public LoginPage logout() {
        tap(LOGOUT);
        return new LoginPage(driver);
    }
}
