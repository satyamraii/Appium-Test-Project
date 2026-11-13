package com.pcloudy.appium.pages;

import io.appium.java_client.android.AndroidDriver;

import org.openqa.selenium.By;

public class PaymentPage extends BasePage {

    private static final By AMOUNT = id("tv_payment_amount");
    private static final By BANK = id("spinner_bank");
    private static final By CARD_TYPE = id("spinner_card_type");
    private static final By CARD_DETAILS = id("layout_card_details");
    private static final By CARD_NUMBER = id("et_card_number");
    private static final By EXPIRY = id("et_expiry");
    private static final By CVV = id("et_cvv");

    public PaymentPage(AndroidDriver driver) {
        super(driver);
        waitForVisible(BANK);
        captureScreen();
    }

    public String getAmount() {
        return textOf(AMOUNT);
    }

    /** e.g. "PAY ₹ 19,194". */
    public String getPayButtonText() {
        return textAfterScroll("btn_pay_now");
    }

    public PaymentPage selectBank(String bank) {
        selectFromDropdown(BANK, bank);
        return this;
    }

    public PaymentPage selectCardType(String cardType) {
        selectFromDropdown(CARD_TYPE, cardType);
        return this;
    }

    public String getSelectedBank() {
        return selectedOption(BANK);
    }

    public String getSelectedCardType() {
        return selectedOption(CARD_TYPE);
    }

    /** Card number, expiry and CVV appear only after a card type is chosen. */
    public boolean areCardFieldsDisplayed() {
        return isDisplayedNow(CARD_DETAILS);
    }

    public PaymentPage enterCard(String number, String expiry, String cvv) {
        type(CARD_NUMBER, number);
        scrollTo("et_expiry");
        type(EXPIRY, expiry);
        type(CVV, cvv);
        return this;
    }

    public OtpPage pay() {
        tapAfterScroll("btn_pay_now");
        return new OtpPage(driver);
    }

    public String payExpectingError() {
        tapAfterScroll("btn_pay_now");
        return textAfterScroll("tv_payment_error");
    }
}
