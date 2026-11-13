package com.pcloudy.appium.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import com.pcloudy.appium.base.BaseTest;
import com.pcloudy.appium.data.TestData;
import com.pcloudy.appium.pages.BookFlightPage;
import com.pcloudy.appium.pages.BookingConfirmationPage;
import com.pcloudy.appium.pages.FlightResultsPage;
import com.pcloudy.appium.pages.FlightResultsPage.FlightOption;
import com.pcloudy.appium.pages.HomePage;
import com.pcloudy.appium.pages.LicensePage;
import com.pcloudy.appium.pages.LoginPage;
import com.pcloudy.appium.pages.OtpPage;
import com.pcloudy.appium.pages.PassengerDetailsPage;
import com.pcloudy.appium.pages.PaymentPage;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.time.LocalDate;

public class PositiveTests extends BaseTest {

    private static final String BOOKING_ID_FORMAT = "PCL\\d{6}";
    private static final String TRANSACTION_ID_FORMAT = "TXN\\d{8}";

    @Test(priority = 1, description = "TC_01: Valid credentials open the End user Licence screen")
    public void validLoginOpensLicenceScreen() {
        LicensePage licence = new LoginPage(driver).loginAs(TestData.USERNAME, TestData.PASSWORD);

        assertEquals(licence.getTitle(), "End user Licence");
        assertTrue(licence.getLicenceText().startsWith("Terms and Conditions"), "Licence text should be shown");
    }

    @Test(priority = 2, description = "TC_02: Accepting the licence opens Home with a welcome message")
    public void acceptingLicenceOpensHome() {
        HomePage home = loginAndAcceptLicence();

        assertEquals(home.getTitle(), "pCloudy Test App");
        assertEquals(home.getWelcomeText(), "Welcome, " + TestData.USERNAME);
        assertTrue(home.isBookFlightDisplayed(), "BOOK A FLIGHT should be shown");
        assertTrue(home.isSingleBookingReferenceDisplayed(), "SINGLE BOOKING REFERENCE should be shown");
    }

    @Test(priority = 3, description = "TC_03: One-way search lists flights for the chosen route")
    public void oneWaySearchShowsFlights() {
        LocalDate depart = TestData.departDate();
        BookFlightPage search = openBookFlight()
                .selectFrom(TestData.MUMBAI)
                .selectTo(TestData.DUBAI)
                .chooseOneWay()
                .setDepartDate(depart);
        assertEquals(search.getDepartDate(), BookFlightPage.formatted(depart));

        FlightResultsPage results = search.searchFlights();

        assertTrue(results.getRoute().matches("BOM\\s+✈\\s+DXB"), "Route was: " + results.getRoute());
        assertEquals(results.getResultsCount(), "5 flights found");
        String summary = results.getSummary();
        assertTrue(summary.contains(BookFlightPage.formatted(depart)), "Summary was: " + summary);
        assertTrue(summary.contains("1 Adult"), "Summary was: " + summary);
        assertTrue(summary.contains("Economy"), "Summary was: " + summary);
    }

    @Test(priority = 4, description = "TC_04: Return search with several travellers shows both dates and passengers")
    public void returnSearchShowsDatesAndTravellers() {
        LocalDate depart = TestData.departDate();
        LocalDate back = TestData.returnDate();

        FlightResultsPage results = openBookFlight()
                .selectFrom(TestData.BANGALORE)
                .selectTo(TestData.NEW_DELHI)
                .chooseReturn()
                .setDepartDate(depart)
                .setReturnDate(back)
                .selectAdults(2)
                .selectChildren(1)
                .selectFlightClass("Premium Economy")
                .searchFlights();

        String summary = results.getSummary();
        assertTrue(summary.contains(BookFlightPage.formatted(depart) + " – " + BookFlightPage.formatted(back)),
                "Summary was: " + summary);
        assertTrue(summary.contains("2 Adults, 1 Child"), "Summary was: " + summary);
        assertTrue(summary.contains("Premium Economy"), "Summary was: " + summary);
    }

    @Test(priority = 5, description = "TC_05: The selected flight and fare are carried to Passenger Details")
    public void selectedFlightCarriedToPassengerDetails() {
        FlightResultsPage results = openBookFlight()
                .selectFrom(TestData.MUMBAI)
                .selectTo(TestData.LONDON)
                .chooseOneWay()
                .setDepartDate(TestData.departDate())
                .searchFlights();
        FlightOption first = results.getFlight(0);

        PassengerDetailsPage passenger = results.selectFlight(0);

        assertTrue(passenger.getSelectedRoute().matches("BOM\\s+✈\\s+LHR"), "Route was: " + passenger.getSelectedRoute());
        assertTrue(passenger.getSelectedFlight().contains(first.airline()));
        assertTrue(passenger.getSelectedFlight().contains(first.flightNumber()));
        assertTrue(passenger.getSelectedSchedule().contains(first.departTime()));
        assertEquals(passenger.getFareTotal(), first.price());
    }

    @Test(priority = 6, description = "TC_06: Card details appear only after choosing Debit/Credit card")
    public void cardFieldsAppearAfterChoosingCardType() {
        PaymentPage payment = reachPayment();
        assertFalse(payment.areCardFieldsDisplayed(), "Card fields should be hidden before a card type is chosen");

        payment.selectBank(TestData.BANK_SBI).selectCardType(TestData.DEBIT_CARD);

        assertTrue(payment.areCardFieldsDisplayed(), "Card fields should be shown after choosing Debit Card");
        assertEquals(payment.getSelectedBank(), TestData.BANK_SBI);
        assertEquals(payment.getSelectedCardType(), TestData.DEBIT_CARD);
        assertEquals(payment.getPayButtonText(), "PAY " + payment.getAmount());
    }

    @Test(priority = 7, description = "TC_07: OTP is sent to the masked mobile number and can be resent")
    public void resendOtpGeneratesNewOtp() {
        OtpPage otp = reachPayment()
                .selectBank(TestData.BANK_HDFC)
                .selectCardType(TestData.CREDIT_CARD)
                .enterCard(TestData.CARD_NUMBER, TestData.validCardExpiry(), TestData.CVV)
                .pay();

        assertEquals(otp.getHeading(), "Verify Payment");
        assertTrue(otp.getMessage().contains("******" + TestData.PHONE.substring(6)), "Message: " + otp.getMessage());
        String first = otp.getOtp();
        assertTrue(first.matches("\\d{6}"), "OTP should be 6 digits: " + first);

        String second = otp.resendOtp().getOtp();

        assertTrue(second.matches("\\d{6}"), "OTP should be 6 digits: " + second);
        assertNotEquals(second, first, "Resend should generate a new OTP");
    }

    @Test(priority = 8, description = "TC_08: End-to-end one-way booking from login to booking confirmation")
    public void endToEndOneWayBooking() {
        LocalDate depart = TestData.departDate();

        // Login -> Licence -> Home -> Search
        FlightResultsPage results = loginAndAcceptLicence()
                .openBookFlight()
                .selectFrom(TestData.MUMBAI)
                .selectTo(TestData.DUBAI)
                .chooseOneWay()
                .setDepartDate(depart)
                .selectAdults(TestData.ADULTS)
                .selectFlightClass(TestData.FLIGHT_CLASS)
                .selectTicketType(TestData.TICKET_TYPE)
                .searchFlights();

        // Results -> Passenger -> Payment -> OTP
        FlightOption flight = results.getFlight(0);
        PaymentPage payment = results.selectFlight(0)
                .enterPassenger(TestData.GIVEN_NAME, TestData.LAST_NAME, TestData.EMAIL, TestData.PHONE)
                .continueToPayment();
        assertEquals(payment.getAmount(), flight.price(), "Amount payable should equal the selected fare");

        OtpPage otp = payment
                .selectBank(TestData.BANK_HDFC)
                .selectCardType(TestData.CREDIT_CARD)
                .enterCard(TestData.CARD_NUMBER, TestData.validCardExpiry(), TestData.CVV)
                .pay();
        BookingConfirmationPage confirmation = otp.enterOtp(otp.getOtp()).verify();

        SoftAssert soft = new SoftAssert();
        soft.assertEquals(confirmation.getTitle(), "Booking Confirmed!");
        soft.assertEquals(confirmation.getSubtitle(), "Your ticket has been booked successfully");
        soft.assertTrue(confirmation.getBookingId().matches(BOOKING_ID_FORMAT), "Booking id: " + confirmation.getBookingId());
        soft.assertEquals(confirmation.getBookingStatus(), "CONFIRMED");
        soft.assertEquals(confirmation.getFrom(), TestData.MUMBAI);
        soft.assertEquals(confirmation.getTo(), TestData.DUBAI);
        soft.assertEquals(confirmation.getAirline(), flight.airline());
        soft.assertEquals(confirmation.getFlightNumber(), flight.flightNumber());
        soft.assertEquals(confirmation.getTripType(), "One-way");
        soft.assertEquals(confirmation.getTravelDate(), BookFlightPage.formatted(depart));
        soft.assertEquals(confirmation.getDepartureTime(), flight.departTime());
        soft.assertEquals(confirmation.getPassengerName(), TestData.GIVEN_NAME + " " + TestData.LAST_NAME);
        soft.assertEquals(confirmation.getEmail(), TestData.EMAIL);
        soft.assertEquals(confirmation.getPhone(), TestData.PHONE);
        soft.assertEquals(confirmation.getTravellers(), TestData.ADULTS + " Adults");
        soft.assertEquals(confirmation.getFlightClass(), TestData.FLIGHT_CLASS);
        soft.assertEquals(confirmation.getTicketType(), TestData.TICKET_TYPE);
        soft.assertEquals(confirmation.getTotalFare(), flight.price());
        soft.assertEquals(confirmation.getPaymentStatus(), "PAID");
        soft.assertEquals(confirmation.getPaymentMethod(), TestData.BANK_HDFC + " • " + TestData.CREDIT_CARD);
        soft.assertEquals(confirmation.getCardNumber(), "XXXX XXXX XXXX " + TestData.cardLast4());
        soft.assertTrue(confirmation.getTransactionId().matches(TRANSACTION_ID_FORMAT),
                "Transaction id: " + confirmation.getTransactionId());
        soft.assertEquals(confirmation.getHappyJourneyMessage(), "Wishes you a very Happy Journey...");
        soft.assertAll();

        HomePage home = confirmation.backToHome();
        assertTrue(home.isBookFlightDisplayed(), "BACK TO HOME should return to the Home screen");
    }

    @Test(priority = 9, description = "TC_09: End-to-end return booking with debit card can be found by its reference")
    public void endToEndReturnBookingFoundByReference() {
        LocalDate depart = TestData.departDate();
        LocalDate back = TestData.returnDate();

        FlightResultsPage results = loginAndAcceptLicence()
                .openBookFlight()
                .selectFrom(TestData.BANGALORE)
                .selectTo(TestData.NEW_DELHI)
                .chooseReturn()
                .setDepartDate(depart)
                .setReturnDate(back)
                .selectAdults(2)
                .selectChildren(1)
                .selectFlightClass("Business")
                .selectTicketType("Flexible")
                .searchFlights();
        FlightOption flight = results.getFlight(0);

        OtpPage otp = results.selectFlight(0)
                .enterPassenger(TestData.GIVEN_NAME, TestData.LAST_NAME, TestData.EMAIL, TestData.PHONE)
                .continueToPayment()
                .selectBank(TestData.BANK_SBI)
                .selectCardType(TestData.DEBIT_CARD)
                .enterCard(TestData.CARD_NUMBER, TestData.validCardExpiry(), TestData.CVV)
                .pay();
        BookingConfirmationPage confirmation = otp.enterOtp(otp.getOtp()).verify();

        String bookingId = confirmation.getBookingId();
        SoftAssert soft = new SoftAssert();
        soft.assertEquals(confirmation.getTitle(), "Booking Confirmed!");
        soft.assertEquals(confirmation.getTripType(), "Return");
        soft.assertEquals(confirmation.getTravelDate(), BookFlightPage.formatted(depart));
        soft.assertEquals(confirmation.getReturnDate(), BookFlightPage.formatted(back));
        soft.assertEquals(confirmation.getTravellers(), "2 Adults, 1 Child");
        soft.assertEquals(confirmation.getFlightClass(), "Business");
        soft.assertEquals(confirmation.getTicketType(), "Flexible");
        soft.assertEquals(confirmation.getTotalFare(), flight.price());
        soft.assertEquals(confirmation.getPaymentMethod(), TestData.BANK_SBI + " • " + TestData.DEBIT_CARD);

        // Look the new booking up again; reference and names are matched ignoring case.
        BookingConfirmationPage details = confirmation.backToHome()
                .openFindBooking()
                .enterDetails(bookingId.toLowerCase(), TestData.GIVEN_NAME.toUpperCase(), TestData.LAST_NAME)
                .findBooking();

        soft.assertEquals(details.getTitle(), "Booking Details");
        soft.assertEquals(details.getBookingId(), bookingId);
        soft.assertEquals(details.getFlightNumber(), flight.flightNumber());
        soft.assertEquals(details.getReturnDate(), BookFlightPage.formatted(back));
        soft.assertEquals(details.getPassengerName(), TestData.GIVEN_NAME + " " + TestData.LAST_NAME);
        soft.assertEquals(details.getPaymentStatus(), "PAID");
        soft.assertAll();
    }

    @Test(priority = 10, description = "TC_10: The pre-loaded booking is found via Single Booking Reference")
    public void preloadedBookingFoundByReference() {
        BookingConfirmationPage details = loginAndAcceptLicence()
                .openFindBooking()
                .enterDetails(TestData.PRELOADED_BOOKING_ID, TestData.PRELOADED_GIVEN_NAME, TestData.PRELOADED_LAST_NAME)
                .findBooking();

        SoftAssert soft = new SoftAssert();
        soft.assertEquals(details.getTitle(), "Booking Details");
        soft.assertEquals(details.getBookingId(), TestData.PRELOADED_BOOKING_ID);
        soft.assertEquals(details.getFrom(), TestData.BANGALORE);
        soft.assertEquals(details.getTo(), TestData.NEW_DELHI);
        soft.assertEquals(details.getAirline(), "pCloudy Air");
        soft.assertEquals(details.getFlightNumber(), "PC 101");
        soft.assertEquals(details.getPassengerName(), "pCloudy User");
        soft.assertEquals(details.getTotalFare(), "₹ 4,850");
        soft.assertAll();
    }
}
