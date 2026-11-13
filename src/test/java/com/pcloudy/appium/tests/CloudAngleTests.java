package com.pcloudy.appium.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.pcloudy.appium.base.BaseTest;
import com.pcloudy.appium.config.Config;
import com.pcloudy.appium.data.TestData;
import com.pcloudy.appium.pages.HomePage;
import com.pcloudy.appium.pages.LoginPage;
import com.pcloudy.appium.utils.ScreenshotUtil;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.appmanagement.ApplicationState;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * CloudAngle capability checks: app management, device interaction and DOM access through Appium.
 * Capabilities the cloud device does not allow (for example adb shell) are reported as skipped, not failed.
 */
public class CloudAngleTests extends BaseTest {

    private static final String APP_PACKAGE = Config.require("app.package");
    private static final String APP_ACTIVITY = Config.require("app.activity");
    private static final By USERNAME = AppiumBy.id(APP_PACKAGE + ":id/et_username");
    private static final By LOGIN_BUTTON = AppiumBy.id(APP_PACKAGE + ":id/btn_login");
    private static final By BOOK_FLIGHT = AppiumBy.id(APP_PACKAGE + ":id/btn_book_flight");
    private static final By PRESS_BUTTON = AppiumBy.id(APP_PACKAGE + ":id/btn_long_press");
    private static final By DRAG_SOURCE = AppiumBy.id(APP_PACKAGE + ":id/tv_drag_source");
    private static final By DROP_ZONE = AppiumBy.id(APP_PACKAGE + ":id/layout_drop_zone");
    private static final By DROP_ZONE_TEXT = AppiumBy.id(APP_PACKAGE + ":id/tv_drop_zone");
    private static final By DRAG_STATUS = AppiumBy.id(APP_PACKAGE + ":id/tv_drag_status");
    private static final By SEARCH_FLIGHTS = AppiumBy.id(APP_PACKAGE + ":id/btn_search_flights");

    // ------------------------------------------------------------------ App Mgmt

    @Test(priority = 1, description = "CA_01: Install APK")
    public void installApk() {
        Path apk = localApk();

        driver.installApp(apk.toString());

        assertTrue(driver.isAppInstalled(APP_PACKAGE), APP_PACKAGE + " should be installed after installApp");
    }

    @Test(priority = 2, description = "CA_02: Install IPA")
    public void installIpa() {
        // The suite drives an Android app through UiAutomator2; an IPA can only be installed on an iOS session.
        throw new SkipException("Install IPA needs an iOS device and XCUITest session; this suite is Android only");
    }

    @Test(priority = 3, description = "CA_03: List installed apps")
    public void listInstalledApps() {
        String packages = shell("pm", "list", "packages");

        assertTrue(packages.contains("package:" + APP_PACKAGE), "Installed packages should include " + APP_PACKAGE);
    }

    @Test(priority = 4, description = "CA_04: Clear app data returns the app to a fresh Login screen")
    public void clearAppData() {
        loginAndAcceptLicence();

        driver.executeScript("mobile: clearApp", Map.of("appId", APP_PACKAGE));
        driver.activateApp(APP_PACKAGE);

        assertTrue(new LoginPage(driver).isDisplayed(), "After clearing data the app should start on Login");
    }

    @Test(priority = 5, description = "CA_05: Kill and relaunch the app")
    public void killAndRelaunchApp() {
        new LoginPage(driver);
        assertEquals(driver.queryAppState(APP_PACKAGE), ApplicationState.RUNNING_IN_FOREGROUND);

        driver.terminateApp(APP_PACKAGE);
        assertEquals(driver.queryAppState(APP_PACKAGE), ApplicationState.NOT_RUNNING, "App should be killed");

        driver.activateApp(APP_PACKAGE);
        assertEquals(driver.queryAppState(APP_PACKAGE), ApplicationState.RUNNING_IN_FOREGROUND, "App should relaunch");
        assertTrue(new LoginPage(driver).isDisplayed(), "Relaunched app should show the Login screen");
    }

    @Test(priority = 6, description = "CA_06: Extract APK metadata (package, version) from the installed app")
    public void extractApkMetadata() {
        String info = shell("dumpsys", "package", APP_PACKAGE);

        assertTrue(info.contains("Package [" + APP_PACKAGE + "]"), "dumpsys should describe " + APP_PACKAGE);
        assertTrue(info.contains("versionName="), "Metadata should include versionName");
        assertTrue(info.contains("versionCode="), "Metadata should include versionCode");
    }

    @Test(priority = 7, description = "CA_07: Uninstall app")
    public void uninstallApp() {
        assertTrue(driver.isAppInstalled(APP_PACKAGE), "App should be installed before uninstalling");

        try {
            assertTrue(driver.removeApp(APP_PACKAGE), "removeApp should report success");
            assertFalse(driver.isAppInstalled(APP_PACKAGE), "App should no longer be installed");
        } finally {
            reinstallIfPossible();
        }
    }

    // ------------------------------------------------------------------ Interact

    @Test(priority = 8, description = "CA_08: Tap elements - a normal tap shows the 'Normal click pressed' toast")
    public void tapElements() {
        driver.findElement(USERNAME).click();
        assertTrue(driver.isKeyboardShown(), "Tapping the username field should focus it and open the keyboard");
        driver.hideKeyboard();

        scrollToPressButton().click();

        assertEquals(waitForToast(), "Normal click pressed");
    }

    @Test(priority = 9, description = "CA_09: Form fill / text input")
    public void formFillTextInput() {
        WebElement username = driver.findElement(USERNAME);
        username.clear();
        username.sendKeys(TestData.USERNAME);

        assertEquals(username.getText(), TestData.USERNAME);
    }

    @Test(priority = 10, description = "CA_10: Long press - holding the button shows the 'Long pressed successfully' toast")
    public void longPress() {
        WebElement button = scrollToPressButton();

        driver.executeScript("mobile: longClickGesture", Map.of(
                "elementId", ((org.openqa.selenium.remote.RemoteWebElement) button).getId(),
                "duration", 800));

        assertEquals(waitForToast(), "Long pressed successfully");
    }

    @Test(priority = 11, description = "CA_11: Drag and drop - dropping the plane inside the box succeeds")
    public void dragAndDropInsideBoxSucceeds() {
        assertEquals(driver.findElement(DRAG_STATUS).getText(), "Drag the plane into the box");
        assertEquals(driver.findElement(DROP_ZONE_TEXT).getText(), "Drop here");

        // With both login fields empty, a successful drop logs in after 0.5 s and leaves this screen.
        // Filling the user name keeps the screen so the drop result can be read.
        driver.findElement(USERNAME).sendKeys(TestData.USERNAME);
        driver.hideKeyboard();

        Rectangle zone = driver.findElement(DROP_ZONE).getRect();
        dragPlaneTo(zone.x + zone.width / 2, zone.y + zone.height / 2);

        assertEquals(driver.findElement(DRAG_STATUS).getText(), "Dropped successfully");
        assertEquals(driver.findElement(DROP_ZONE_TEXT).getText(), "Dropped \u2713");

        sleep(6000); // Wait for the login to complete and the screen to change.
    }

    @Test(priority = 12, description = "NCA_11: Drag and drop - dropping the plane outside the box fails")
    public void dragAndDropOutsideBoxFails() {
        Rectangle zone = driver.findElement(DROP_ZONE).getRect();
        // A blank spot above the box, between the LOG IN button and the drop zone.
        dragPlaneTo(zone.x + zone.width / 2, zone.y - 120);

        assertEquals(driver.findElement(DRAG_STATUS).getText(), "Dropped outside the box. Try again");
        assertEquals(driver.findElement(DROP_ZONE_TEXT).getText(), "Drop here", "The box must stay empty");
    }

    @Test(priority = 13, description = "CA_12: Scroll (X and Y)")
    public void scroll() {
        openBookFlight();
        By scrollable = AppiumBy.androidUIAutomator("new UiSelector().scrollable(true)");
        WebElement list = driver.findElement(scrollable);
        String id = ((org.openqa.selenium.remote.RemoteWebElement) list).getId();

        for (int i = 0; i < 5 && driver.findElements(SEARCH_FLIGHTS).stream().noneMatch(WebElement::isDisplayed); i++) {
            driver.executeScript("mobile: scrollGesture",
                    Map.of("elementId", id, "direction", "down", "percent", 0.75));
        }
        assertTrue(driver.findElement(SEARCH_FLIGHTS).isDisplayed(), "Vertical scroll should reach SEARCH FLIGHTS");

        // Horizontal scroll: the call must be accepted even when the content does not scroll sideways.
        driver.executeScript("mobile: scrollGesture", Map.of("elementId", id, "direction", "right", "percent", 0.5));
        assertTrue(driver.findElement(SEARCH_FLIGHTS).isDisplayed(), "Horizontal scroll must not break the screen");
    }

    @Test(priority = 14, description = "CA_13: Back / navigation")
    public void backNavigation() {
        HomePage home = loginAndAcceptLicence();
        home.openBookFlight();

        driver.navigate().back();

        assertTrue(new HomePage(driver).isBookFlightDisplayed(), "Back from Book Flight should return to Home");
    }

    @Test(priority = 15, description = "CA_14: Hide keyboard")
    public void hideKeyboard() {
        driver.findElement(USERNAME).click();
        assertTrue(driver.isKeyboardShown(), "Keyboard should be open");

        driver.hideKeyboard();

        assertFalse(driver.isKeyboardShown(), "Keyboard should be hidden");
    }

    @Test(priority = 20, description = "CA_19: Press Home, scroll up to open the app drawer, then return to the app")
    public void homeThenScrollUp() {
        new LoginPage(driver);

        driver.pressKey(new KeyEvent(AndroidKey.HOME));
        waitUntilAppInBackground();
        String launcher = driver.getCurrentPackage();
        String homeScreen = driver.getPageSource();
        assertNotEquals(launcher, APP_PACKAGE, "Home should send the app to the background");

        Dimension screen = driver.manage().window().getSize();
        driver.executeScript("mobile: swipeGesture", Map.of(
                "left", screen.width / 4,
                "top", screen.height / 2,
                "width", screen.width / 2,
                "height", screen.height / 3,
                "direction", "up",
                "percent", 0.9));

        assertEquals(driver.getCurrentPackage(), launcher, "Scrolling up should stay on the launcher");
        assertNotEquals(driver.getPageSource(), homeScreen, "Scrolling up on Home should open the app drawer");

        driver.activateApp(APP_PACKAGE);
        assertTrue(new LoginPage(driver).isDisplayed(), "The app should come back after leaving it with Home");
    }

    // ------------------------------------------------------------------ DOM

    @Test(priority = 16, description = "CA_15: Page source (XML tree)")
    public void pageSource() {
        String source = driver.getPageSource();

        assertNotNull(source);
        assertTrue(source.startsWith("<?xml") || source.startsWith("<hierarchy"), "Page source should be an XML tree");
        assertTrue(source.contains("resource-id=\"" + APP_PACKAGE + ":id/btn_login\""), "Tree should contain the login button");
    }

    @Test(priority = 17, description = "CA_16: Screenshot")
    public void screenshot() throws IOException {
        byte[] png = driver.getScreenshotAs(OutputType.BYTES);

        assertTrue(png.length > 1000, "Screenshot should not be empty");
        assertEquals(png[1], (byte) 'P', "Screenshot should be a PNG image");
        ScreenshotUtil.captureScreenShots(driver, "CloudAngle_screenshot");
    }

    @Test(priority = 18, description = "CA_17: Element bounds")
    public void elementBounds() {
        loginAndAcceptLicence();
        Rectangle bounds = driver.findElement(BOOK_FLIGHT).getRect();
        Dimension screen = driver.manage().window().getSize();

        assertTrue(bounds.width > 0 && bounds.height > 0, "Element should have a size: " + bounds);
        assertTrue(bounds.x >= 0 && bounds.y >= 0, "Element should start on screen: " + bounds);
        assertTrue(bounds.x + bounds.width <= screen.width && bounds.y + bounds.height <= screen.height,
                "Element " + bounds + " should fit the screen " + screen);
    }

    @Test(priority = 19, description = "CA_18: Current activity / package")
    public void currentActivityAndPackage() {
        new LoginPage(driver);

        assertEquals(driver.getCurrentPackage(), APP_PACKAGE);
        String activity = driver.currentActivity();
        assertTrue(activity.endsWith(APP_ACTIVITY.substring(APP_ACTIVITY.lastIndexOf('.') + 1)),
                "Current activity " + activity + " should be " + APP_ACTIVITY);
    }

    // ------------------------------------------------------------------ helpers

    private void waitUntilAppInBackground() {
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> !APP_PACKAGE.equals(driver.getCurrentPackage()));
    }

    /** Brings TAP OR LONG PRESS into view (it is below the fold on smaller screens). */
    private WebElement scrollToPressButton() {
        if (driver.findElements(PRESS_BUTTON).isEmpty()) {
            return driver.findElement(AppiumBy.androidUIAutomator(
                    "new UiScrollable(new UiSelector().scrollable(true))"
                            + ".scrollIntoView(new UiSelector().resourceId(\"" + APP_PACKAGE + ":id/btn_long_press\"))"));
        }
        return driver.findElement(PRESS_BUTTON);
    }

    /** Text of the toast currently shown. Toasts last about 2 s, so call this right after the action. */
    private String waitForToast() {
        return new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.presenceOfElementLocated(AppiumBy.xpath("//android.widget.Toast")))
                .getText();
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void dragPlaneTo(int endX, int endY) {
        WebElement source = driver.findElement(DRAG_SOURCE);
        driver.executeScript("mobile: dragGesture", Map.of(
                "elementId", ((org.openqa.selenium.remote.RemoteWebElement) source).getId(),
                "endX", endX,
                "endY", endY,
                "speed", 600));
    }

    /** Runs an adb shell command through Appium; skips the test when the device does not allow it. */
    private String shell(String command, String... args) {
        try {
            Object output = driver.executeScript("mobile: shell", Map.of("command", command, "args", List.of(args)));
            return String.valueOf(output);
        } catch (WebDriverException e) {
            throw new SkipException("'mobile: shell' is not allowed on this device/hub: " + firstLine(e.getMessage()));
        }
    }

    /** The APK configured as app.path; skips when it is not present on this machine (e.g. cloud-only runs). */
    private Path localApk() {
        String appPath = Config.get("app.path");
        Path apk = Paths.get(appPath).toAbsolutePath().normalize();
        if (appPath.isEmpty() || !Files.exists(apk)) {
            throw new SkipException("APK not found at " + apk + "; set app.path to a local APK to run this test");
        }
        return apk;
    }

    private void reinstallIfPossible() {
        try {
            driver.installApp(localApk().toString());
        } catch (RuntimeException e) {
            // Best effort only: every test starts a new session, which installs the app again.
        }
    }

    private static String firstLine(String message) {
        return message == null ? "" : message.lines().findFirst().orElse("");
    }
}
