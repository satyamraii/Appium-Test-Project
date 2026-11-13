package com.pcloudy.appium.pages;

import com.pcloudy.appium.data.TestData;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookFlightPage extends BasePage {

    private static final By FROM = id("spinner_from");
    private static final By TO = id("spinner_to");
    private static final By RETURN = id("rb_return");
    private static final By ONE_WAY = id("rb_one_way");
    private static final By DEPART_DATE = id("tv_departs_date");
    private static final By RETURN_DATE = id("tv_returns_date");
    private static final By ADULTS = id("spinner_adults");
    private static final By CHILDREN = id("spinner_children");
    private static final By INFANTS = id("spinner_infants");
    private static final By FLIGHT_CLASS = id("spinner_flight_class");
    private static final By TICKET_TYPE = id("spinner_ticket_type");
    private static final By ERROR = id("tv_search_error");

    // Android DatePicker dialog
    private static final By PICKER_YEAR = androidId("date_picker_header_year");
    private static final By PICKER_DATE = androidId("date_picker_header_date");
    private static final By PICKER_NEXT_MONTH = androidId("next");
    private static final By PICKER_OK = androidId("button1");
    private static final DateTimeFormatter PICKER_MONTH = DateTimeFormatter.ofPattern("MMM", Locale.US);
    private static final DateTimeFormatter PICKER_DAY = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US);

    public BookFlightPage(AndroidDriver driver) {
        super(driver);
        waitForVisible(FROM);
        captureScreen();
    }

    public BookFlightPage selectFrom(String city) {
        selectFromDropdown(FROM, city);
        return this;
    }

    public BookFlightPage selectTo(String city) {
        selectFromDropdown(TO, city);
        return this;
    }

    public BookFlightPage chooseOneWay() {
        tap(ONE_WAY);
        return this;
    }

    public BookFlightPage chooseReturn() {
        tap(RETURN);
        return this;
    }

    public BookFlightPage setDepartDate(LocalDate date) {
        tap(DEPART_DATE);
        pickDate(date);
        return this;
    }

    public BookFlightPage setReturnDate(LocalDate date) {
        tap(RETURN_DATE);
        pickDate(date);
        return this;
    }

    public BookFlightPage selectAdults(int count) {
        selectFromDropdown(ADULTS, String.valueOf(count));
        return this;
    }

    public BookFlightPage selectChildren(int count) {
        selectFromDropdown(CHILDREN, String.valueOf(count));
        return this;
    }

    public BookFlightPage selectInfants(int count) {
        selectFromDropdown(INFANTS, String.valueOf(count));
        return this;
    }

    public BookFlightPage selectFlightClass(String flightClass) {
        selectFromDropdown(FLIGHT_CLASS, flightClass);
        return this;
    }

    public BookFlightPage selectTicketType(String ticketType) {
        selectFromDropdown(TICKET_TYPE, ticketType);
        return this;
    }

    public String getDepartDate() {
        return textOf(DEPART_DATE);
    }

    public String getReturnDate() {
        return textOf(RETURN_DATE);
    }

    public String getSelectedFrom() {
        return selectedOption(FROM);
    }

    public FlightResultsPage searchFlights() {
        tapAfterScroll("btn_search_flights");
        return new FlightResultsPage(driver);
    }

    /** Searches with invalid input and returns the validation message. */
    public String searchExpectingError() {
        tapAfterScroll("btn_search_flights");
        return textOf(ERROR);
    }

    /**
     * Picks a date in the open DatePicker. The header shows the currently selected date, which tells
     * how many months to page forward before tapping the day.
     */
    private void pickDate(LocalDate target) {
        int year = Integer.parseInt(textOf(PICKER_YEAR));
        // The header is e.g. "Wed, Sep 30", "Wed, 30 Sep" or "Wed, 30 Sept" depending on the device locale,
        // so only the first three letters of the month word are read.
        String header = textOf(PICKER_DATE);
        Matcher monthWord = Pattern.compile("[A-Za-z]{3,}").matcher(header.substring(header.indexOf(',') + 1));
        if (!monthWord.find()) {
            throw new IllegalStateException("No month found in DatePicker header: " + header);
        }
        Month shown = Month.from(PICKER_MONTH.parse(monthWord.group().substring(0, 3)));

        long monthsAhead = ChronoUnit.MONTHS.between(YearMonth.of(year, shown), YearMonth.from(target));
        if (monthsAhead < 0) {
            throw new IllegalArgumentException("Date " + target + " is before the earliest selectable date");
        }
        for (int i = 0; i < monthsAhead; i++) {
            tap(PICKER_NEXT_MONTH);
        }

        // Each day cell's content-desc is its full date, e.g. "15 October 2026".
        tap(AppiumBy.accessibilityId(target.format(PICKER_DAY)));
        tap(PICKER_OK);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(PICKER_OK));
    }

    public static String formatted(LocalDate date) {
        return date.format(TestData.APP_DATE);
    }
}
