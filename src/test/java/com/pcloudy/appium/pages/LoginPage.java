package com.pcloudy.appium.pages;

import io.appium.java_client.android.AndroidDriver;

import org.openqa.selenium.By;

public class LoginPage extends BasePage {

    private static final By USERNAME = id("et_username");
    private static final By PASSWORD = id("et_password");
    private static final By LOGIN_BUTTON = id("btn_login");
    private static final By ERROR = id("tv_login_error");

    public LoginPage(AndroidDriver driver) {
        super(driver);
        waitForVisible(USERNAME);
        captureScreen();
    }

    public LoginPage enterUsername(String username) {
        type(USERNAME, username);
        return this;
    }

    public LoginPage enterPassword(String password) {
        type(PASSWORD, password);
        return this;
    }

    public LicensePage loginAs(String username, String password) {
        enterUsername(username).enterPassword(password);
        tap(LOGIN_BUTTON);
        return new LicensePage(driver);
    }

    /** Submits credentials that should be rejected, staying on this screen. */
    public LoginPage loginExpectingError(String username, String password) {
        enterUsername(username).enterPassword(password);
        tap(LOGIN_BUTTON);
        waitForVisible(ERROR);
        return this;
    }

    public String getErrorMessage() {
        return textOf(ERROR);
    }

    public boolean isDisplayed() {
        return isDisplayedNow(LOGIN_BUTTON);
    }
}
