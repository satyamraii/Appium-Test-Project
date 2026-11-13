package com.pcloudy.appium.driver;

import com.pcloudy.appium.config.Config;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/** Creates an Android session either on a local Appium server or on pCloudy, based on "execution". */
public final class DriverFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DriverFactory.class);

    private DriverFactory() {
    }

    public static AndroidDriver createDriver() {
        UiAutomator2Options options = new UiAutomator2Options()
                .setAppPackage(Config.require("app.package"))
                .setAppActivity(Config.require("app.activity"))
                // Clear app data before each session so every test starts on the Login screen.
                .setNoReset(false)
                .setAutoGrantPermissions(true)
                .setNewCommandTimeout(Duration.ofSeconds(Config.getInt("timeout.new.command.seconds")));

        String execution = Config.require("execution");
        switch (execution.toLowerCase()) {
            case "local": {
                URL url = toUrl(Config.require("appium.url"));
                LOG.info("Execution mode: LOCAL - using local Appium server at {}", url);
                return new AndroidDriver(url, withLocalOptions(options));
          }
            case "pcloudy": {
                URL url = toUrl(Config.require("pcloudy.hub.url"));
                LOG.info("Execution mode: PCLOUDY - using pCloudy Appium hub at {}", url);
                return new AndroidDriver(url, withPCloudyOptions(options));
            }
            default:
                throw new IllegalStateException("Unknown execution '" + execution + "', use local or pcloudy");
        }
    }

    private static UiAutomator2Options withLocalOptions(UiAutomator2Options options) {
        options.setDeviceName(Config.require("device.name"));

        String udid = Config.get("device.udid");
        if (!udid.isEmpty()) {
            options.setUdid(udid);
        }

        String appPath = Config.get("app.path");
        if (!appPath.isEmpty()) {
            Path apk = Paths.get(appPath).toAbsolutePath().normalize();
            if (!Files.exists(apk)) {
                throw new IllegalStateException("APK not found at " + apk + ". Build the app or fix app.path.");
            }
            options.setApp(apk.toString());
        }
        return options;
    }

    private static UiAutomator2Options withPCloudyOptions(UiAutomator2Options options) {
    	
        options.setPlatformName(Config.require("pcloudy.platform.name"));
        options.setPlatformVersion(Config.require("pcloudy.platform.version"));
        options.amend("appium:launchTimeout", Config.getInt("pcloudy.launch.timeout.ms"));
        options.amend("appium:pCloudy_ApplicationName", Config.require("pcloudy.app.name"));

        // Labels shown for the session in pCloudy reports.
        options.amend("appium:project", Config.require("pcloudy.project"));
        options.amend("appium:build", Config.require("pcloudy.build"));
        options.amend("appium:name", Config.require("pcloudy.session.name"));
        options.amend("appium:tag", Config.require("pcloudy.tag"));

        Map<String, Object> pcloudyOptions = new HashMap<>();
        pcloudyOptions.put("pCloudy_Username", Config.require("pcloudy.username"));
        pcloudyOptions.put("pCloudy_ApiKey", Config.require("pcloudy.api.key"));
        pcloudyOptions.put("pCloudy_DeviceManufacturer", Config.require("pcloudy.device.manufacturer"));
    //    pcloudyOptions.put("pCloudy_DevicePlatformVersion", Config.require("pcloudy.platform.version"));
        pcloudyOptions.put("pCloudy_WildNet", false);
        pcloudyOptions.put("pCloudy_EnableVideo", Config.getBoolean("pcloudy.enable.video"));
        pcloudyOptions.put("pCloudy_EnablePerformanceData", false);
        pcloudyOptions.put("pCloudy_EnableDeviceLogs", Config.getBoolean("pcloudy.enable.device.logs"));
        pcloudyOptions.put("Pcloudy_DurationInMinutes", Config.getInt("pcloudy.duration.minutes")); // pCloudy booking time limit
        pcloudyOptions.put("appiumVersion", Config.require("pcloudy.appium.version"));

        String device = Config.get("pcloudy.device.full.name");
        if (!device.isEmpty()) {
            pcloudyOptions.put("pCloudy_DeviceFullName", device);
        }
        options.setCapability("pcloudy:options", pcloudyOptions);
        return options;
    }

    private static URL toUrl(String url) {
        try {
            return URI.create(url).toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL: " + url, e);
        }
    }
}
