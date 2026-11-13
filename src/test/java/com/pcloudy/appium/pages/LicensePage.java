package com.pcloudy.appium.pages;

import io.appium.java_client.android.AndroidDriver;

import org.openqa.selenium.By;

public class LicensePage extends BasePage {

    private static final By TITLE = id("tv_license_title");
    private static final By LICENCE_TEXT = id("tv_license_text");
    private static final By ACCEPT = id("btn_accept");
    private static final By REFUSE = id("btn_refuse");

    public LicensePage(AndroidDriver driver) {
        super(driver);
        waitForVisible(TITLE);
        captureScreen();
    }

    public String getTitle() {
        return textOf(TITLE);
    }

    public String getLicenceText() {
        return textOf(LICENCE_TEXT);
    }

    public HomePage accept() {
        tap(ACCEPT);
        return new HomePage(driver);
    }

    /** Refuses the licence and returns the toast message shown. */
    public String refuseAndReadToast() {
        tap(REFUSE);
        return waitForToast();
    }

    public boolean isDisplayed() {
        return isDisplayedNow(TITLE) && isDisplayedNow(ACCEPT);
    }
}
