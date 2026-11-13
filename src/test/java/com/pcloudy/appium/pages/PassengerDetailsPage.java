package com.pcloudy.appium.pages;

import io.appium.java_client.android.AndroidDriver;

import org.openqa.selenium.By;

public class PassengerDetailsPage extends BasePage {

    private static final By SELECTED_ROUTE = id("tv_selected_route");
    private static final By SELECTED_FLIGHT = id("tv_selected_flight");
    private static final By SELECTED_SCHEDULE = id("tv_selected_schedule");
    private static final By FARE_TOTAL = id("tv_fare_total");
    private static final By GIVEN_NAME = id("et_given_name");
    private static final By LAST_NAME = id("et_last_name");
    private static final By EMAIL = id("et_email");
    private static final By PHONE = id("et_phone");
    private static final By ERROR = id("tv_passenger_error");

    public PassengerDetailsPage(AndroidDriver driver) {
        super(driver);
        waitForVisible(FARE_TOTAL);
        captureScreen();
    }

    public String getSelectedRoute() {
        return textOf(SELECTED_ROUTE);
    }

    /** e.g. "AeroNova  •  AN 362". */
    public String getSelectedFlight() {
        return textOf(SELECTED_FLIGHT);
    }

    public String getSelectedSchedule() {
        return textOf(SELECTED_SCHEDULE);
    }

    public String getFareTotal() {
        return textOf(FARE_TOTAL);
    }

    public PassengerDetailsPage enterPassenger(String givenName, String lastName, String email, String phone) {
        type(GIVEN_NAME, givenName);
        type(LAST_NAME, lastName);
        scrollTo("et_email");
        type(EMAIL, email);
        scrollTo("et_phone");
        type(PHONE, phone);
        return this;
    }

    public PaymentPage continueToPayment() {
        tapAfterScroll("btn_continue_to_payment");
        return new PaymentPage(driver);
    }

    public String continueExpectingError() {
        tapAfterScroll("btn_continue_to_payment");
        return textAfterScroll("tv_passenger_error");
    }
}
