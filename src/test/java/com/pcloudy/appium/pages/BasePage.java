package com.pcloudy.appium.pages;

import com.pcloudy.appium.config.Config;
import com.pcloudy.appium.utils.ScreenshotUtil;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.testng.Reporter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

/** Wait, input, scroll and dropdown helpers shared by all page objects. */
public abstract class BasePage {

    private static final String APP_ID_PREFIX = Config.require("app.package") + ":id/";

    protected final AndroidDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(AndroidDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(Config.getInt("timeout.explicit.seconds")));
    }

    /** Locator for an app resource id, e.g. id("btn_login"). */
    protected static By id(String resourceName) {
        return AppiumBy.id(APP_ID_PREFIX + resourceName);
    }

    /** Locator for an Android framework id, e.g. androidId("button1"). */
    protected static By androidId(String resourceName) {
        return AppiumBy.id("android:id/" + resourceName);
    }

    /** Screenshot of the screen a page object has just loaded. */
    protected void captureScreen() {
        capture("screen_" + getClass().getSimpleName());
    }

    /** Never fails a test: a missed screenshot is only logged. */
    protected void capture(String label) {
        try {
            ScreenshotUtil.captureScreenShots(driver, label);
        } catch (IOException | RuntimeException e) {
            Reporter.log("Could not save screenshot '" + label + "': " + e.getMessage(), true);
        }
    }

    private static String nameOf(By locator) {
        return locator.toString().replaceAll(".*:id/", "").replaceAll("[^A-Za-z0-9]+", "_");
    }

    protected WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void tap(By locator) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        // Captured before the click so a short-lived toast is not missed afterwards.
        capture("tap_" + nameOf(locator));
        element.click();
    }

    /** Scrolls the element into view first, for controls that may be below the fold. */
    protected void tapAfterScroll(String resourceName) {
        hideKeyboard();
        WebElement element = scrollTo(resourceName);
        capture("tap_" + resourceName);
        element.click();
    }

    protected void type(By locator, String text) {
        WebElement field = waitForVisible(locator);
        field.clear();
        field.sendKeys(text);
        hideKeyboard();
        capture("type_" + nameOf(locator));
    }

    protected String textOf(By locator) {
        return waitForVisible(locator).getText();
    }

    /** Reads text of an element that may need scrolling to reach. */
    protected String textAfterScroll(String resourceName) {
        return scrollTo(resourceName).getText();
    }

    /** True if the element is on screen right now; does not wait. */
    protected boolean isDisplayedNow(By locator) {
        List<WebElement> found = driver.findElements(locator);
        return !found.isEmpty() && found.get(0).isDisplayed();
    }

    protected void hideKeyboard() {
        try {
            if (driver.isKeyboardShown()) {
                driver.hideKeyboard();
            }
        } catch (WebDriverException ignored) {
            // Some devices report the keyboard state unreliably; nothing to hide.
        }
    }

    /** Returns the element, scrolling the screen when it is not already visible. */
    protected WebElement scrollTo(String resourceName) {
        By locator = id(resourceName);
        if (isDisplayedNow(locator)) {
            return driver.findElement(locator);
        }
        return driver.findElement(AppiumBy.androidUIAutomator(
                "new UiScrollable(new UiSelector().scrollable(true))"
                        + ".scrollIntoView(new UiSelector().resourceId(\"" + APP_ID_PREFIX + resourceName + "\"))"));
    }

    /** Opens a Spinner and picks the option with the given text, scrolling the list if needed. */
    protected void selectFromDropdown(By spinner, String option) {
        tap(spinner);
        capture("dropdown_open_" + nameOf(spinner));
        By optionLocator = AppiumBy.androidUIAutomator("new UiSelector().text(\"" + option + "\")");
        waitForVisible(AppiumBy.className("android.widget.ListView"));
        if (!isDisplayedNow(optionLocator)) {
            driver.findElement(AppiumBy.androidUIAutomator(
                    "new UiScrollable(new UiSelector().className(\"android.widget.ListView\"))"
                            + ".scrollIntoView(new UiSelector().text(\"" + option + "\"))"));
        }
        driver.findElement(optionLocator).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(AppiumBy.className("android.widget.ListView")));
        capture("selected_" + nameOf(spinner));
    }

    /** The text currently shown by a Spinner. */
    protected String selectedOption(By spinner) {
        return waitForVisible(spinner).findElement(androidId("text1")).getText();
    }

    /**
     * Waits for a toast and returns its text. Toasts disappear after ~2 seconds, so call this immediately.
     * UiAutomator2 exposes a toast as a synthetic node that is present but not "displayed".
     */
    public String waitForToast() {
        return wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.xpath("//android.widget.Toast")))
                .getText();
    }
}
