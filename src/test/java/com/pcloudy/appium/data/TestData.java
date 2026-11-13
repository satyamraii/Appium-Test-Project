package com.pcloudy.appium.data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Test inputs and the values the app is expected to display. */
public final class TestData {

    public static final String USERNAME = "pCloudy@user";
    public static final String PASSWORD = "Pcloudy@123";
    public static final String WRONG_PASSWORD = "Wrong@123";

    public static final String BANGALORE = "Bangalore, India (BLR)";
    public static final String NEW_DELHI = "New Delhi, India (DEL)";
    public static final String MUMBAI = "Mumbai, India (BOM)";
    public static final String DUBAI = "Dubai, UAE (DXB)";
    public static final String LONDON = "London, GB (LHR)";

    public static final String GIVEN_NAME = "Anjali";
    public static final String LAST_NAME = "Y";
    public static final String EMAIL = "anjali.y@gmail.com";
    public static final String PHONE = "9876543210";

    public static final String FLIGHT_CLASS = "Business";
    public static final String TICKET_TYPE = "Flexible";
    public static final int ADULTS = 2;
    

    public static final String BANK_HDFC = "HDFC Bank";
    public static final String BANK_SBI = "State Bank of India (SBI)";
    public static final String DEBIT_CARD = "Debit Card";
    public static final String CREDIT_CARD = "Credit Card";
    public static final String CARD_NUMBER = "5432109876543210";
    public static final String CVV = "453";
    public static final String EXPIRED_CARD_EXPIRY = "01/30";

    /** Booking that the app creates on first launch. */
    public static final String PRELOADED_BOOKING_ID = "PCL100001";
    public static final String PRELOADED_GIVEN_NAME = "pCloudy";
    public static final String PRELOADED_LAST_NAME = "User";

    /** Date format used everywhere in the app, e.g. "15 Oct 2026". */
    public static final DateTimeFormatter APP_DATE = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US);

    private TestData() {
    }

    /** Today plus 15 days (30 Sep -> 15 Oct), so the date is always in the future and selectable. */
    public static LocalDate departDate() {
        return LocalDate.now().plusDays(15);
    }

    public static LocalDate returnDate() {
        return departDate().plusDays(5);
    }

    /** A card expiry three years ahead, e.g. "12/29". */
    public static String validCardExpiry() {
        return String.format(Locale.US, "12/%02d", (LocalDate.now().getYear() + 3) % 100);
    }

    public static String cardLast4() {
        return CARD_NUMBER.substring(CARD_NUMBER.length() - 4);
    }
}
