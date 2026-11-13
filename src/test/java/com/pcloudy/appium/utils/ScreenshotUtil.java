package com.pcloudy.appium.utils;

import io.appium.java_client.android.AndroidDriver;

import org.openqa.selenium.OutputType;
import org.testng.Reporter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/** Saves screenshots into the "screenshot" folder as NNN_label_dd-MMM-yyyy__hh_mm_ssaa.png, numbered in order. */
public final class ScreenshotUtil {

    private static final Path FOLDER = Paths.get("screenshot");

    private static final AtomicInteger COUNTER = new AtomicInteger();

    private ScreenshotUtil() {
    }

    public static void captureScreenShots(AndroidDriver driver) throws IOException {
        captureScreenShots(driver, "");
    }

    /** The optional label is prefixed to the timestamp so a failed test is easy to spot. */
    public static void captureScreenShots(AndroidDriver driver, String label) throws IOException {
        Files.createDirectories(FOLDER);
        String stamp = new SimpleDateFormat("dd-MMM-yyyy__hh_mm_ssaa").format(new Date());
        String name = label.replaceAll("[^A-Za-z0-9_-]+", "_");
        if (name.length() > 60) {
            name = name.substring(0, 60);
        }
        // The running number keeps steps in order and stops two shots in the same second overwriting each other.
        String file_name = String.format("%03d_%s%s.png", COUNTER.incrementAndGet(), name.isEmpty() ? "" : name + "_", stamp);
        Path file = FOLDER.resolve(file_name);
        Files.write(file, driver.getScreenshotAs(OutputType.BYTES));
        Reporter.log("Screenshot: " + file.toAbsolutePath(), true);
    }
}
