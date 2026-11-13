package com.pcloudy.appium.pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class FlightResultsPage extends BasePage {

    private static final By ROUTE = id("tv_route");
    private static final By SUMMARY = id("tv_search_summary");
    private static final By RESULTS_COUNT = id("tv_results_count");

    public FlightResultsPage(AndroidDriver driver) {
        super(driver);
        waitForVisible(RESULTS_COUNT);
        captureScreen();
    }

    /** e.g. "BOM  ✈  DXB". */
    public String getRoute() {
        return textOf(ROUTE);
    }

    /** e.g. "15 Oct 2026  •  1 Adult  •  Economy". */
    public String getSummary() {
        return textOf(SUMMARY);
    }

    /** e.g. "5 flights found". */
    public String getResultsCount() {
        return textOf(RESULTS_COUNT);
    }

    /** Reads the flight card at the given position (0 = first result). */
    public FlightOption getFlight(int index) {
        WebElement card = waitForVisible(AppiumBy.accessibilityId("card_flight_" + index));
        return new FlightOption(
                card.findElement(id("tv_airline")).getText(),
                card.findElement(id("tv_flight_number")).getText(),
                card.findElement(id("tv_depart_time")).getText(),
                card.findElement(id("tv_arrive_time")).getText(),
                card.findElement(id("tv_price")).getText());
    }

    public PassengerDetailsPage selectFlight(int index) {
        tap(AppiumBy.accessibilityId("btn_select_flight_" + index));
        return new PassengerDetailsPage(driver);
    }

    /** Snapshot of one search result. */
    public record FlightOption(String airline, String flightNumber, String departTime, String arriveTime,
                               String price) {
    }
}
