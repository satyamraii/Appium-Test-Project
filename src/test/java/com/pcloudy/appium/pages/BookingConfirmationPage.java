package com.pcloudy.appium.pages;

import io.appium.java_client.android.AndroidDriver;

import org.openqa.selenium.By;

/**
 * Shown after payment ("Booking Confirmed!") and when an existing booking is opened from
 * Single Booking Reference ("Booking Details"). Lower cards are reached by scrolling.
 */
public class BookingConfirmationPage extends BasePage {

    private static final By BOOKING_ID = id("tv_booking_id");

    public BookingConfirmationPage(AndroidDriver driver) {
        super(driver);
        waitForVisible(BOOKING_ID);
        captureScreen();
    }

    public String getTitle() {
        return textAfterScroll("tv_confirmation_title");
    }

    public String getSubtitle() {
        return textAfterScroll("tv_confirmation_subtitle");
    }

    public String getBookingId() {
        return textAfterScroll("tv_booking_id");
    }

    public String getBookingStatus() {
        return textAfterScroll("tv_booking_status");
    }

    public String getFrom() {
        return textAfterScroll("tv_from");
    }

    public String getTo() {
        return textAfterScroll("tv_to");
    }

    public String getAirline() {
        return textAfterScroll("tv_airline");
    }

    public String getFlightNumber() {
        return textAfterScroll("tv_flight_number");
    }

    public String getTripType() {
        return textAfterScroll("tv_trip_type");
    }

    public String getTravelDate() {
        return textAfterScroll("tv_travel_date");
    }

    public String getReturnDate() {
        return textAfterScroll("tv_return_date");
    }

    public String getDepartureTime() {
        return textAfterScroll("tv_departure_time");
    }

    public String getPassengerName() {
        return textAfterScroll("tv_passenger_name");
    }

    public String getEmail() {
        return textAfterScroll("tv_email");
    }

    public String getPhone() {
        return textAfterScroll("tv_phone");
    }

    public String getTravellers() {
        return textAfterScroll("tv_passengers");
    }

    public String getFlightClass() {
        return textAfterScroll("tv_flight_class");
    }

    public String getTicketType() {
        return textAfterScroll("tv_ticket_type");
    }

    public String getTotalFare() {
        return textAfterScroll("tv_total_fare");
    }

    public String getPaymentStatus() {
        return textAfterScroll("tv_payment_status");
    }

    public String getPaymentMethod() {
        return textAfterScroll("tv_payment_method");
    }

    public String getCardNumber() {
        return textAfterScroll("tv_card_number");
    }

    public String getTransactionId() {
        return textAfterScroll("tv_transaction_id");
    }

    public String getHappyJourneyMessage() {
        return textAfterScroll("tv_happy_journey");
    }

    public HomePage backToHome() {
        tapAfterScroll("btn_back_to_home");
        return new HomePage(driver);
    }
}
