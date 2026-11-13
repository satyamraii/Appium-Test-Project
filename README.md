# Appium-Test-Project

Appium (Java) + TestNG + Maven tests for the **pCloudy Flight Booking** demo app (`../pCloudy-Test-App_Project`).
Every screen has its own page class, and every test starts a fresh session on the Login screen.

## Prerequisites

- JDK 17+ and Maven 3.9+
- Appium 2 with the UiAutomator2 driver (`appium driver install uiautomator2`)
- An emulator or device on adb, or a pCloudy account
- The app APK, built with `./gradlew assembleDebug` in `../pCloudy-Test-App_Project`

## Run

```bash
# 1. Start Appium (local runs only)
appium

# 2. Run the whole suite (testng.xml)
mvn test

# One class, or one test
mvn test -Dtest=NegativeTests
mvn test -Dtest=CloudAngleTests
mvn test -Dtest='PositiveTests#endToEndOneWayBooking'

# A specific device
mvn test -Ddevice.udid=emulator-5554
```

### Run a single test locally (beginner steps)

Example: the end-to-end one-way booking test, `endToEndOneWayBooking` in `PositiveTests` (TC_08).

1. Start an emulator or connect a device, and check it shows up with `adb devices`.
2. In a separate terminal, start Appium: `appium`
3. In the `Appium-Test-Project` folder, run:

```bash
mvn test -Dtest='PositiveTests#endToEndOneWayBooking'
```

The format is `-Dtest='ClassName#methodName'`. Keep the quotes, because `#` is a special character in some shells. To run several tests from one class, join them with commas: `-Dtest='PositiveTests#endToEndOneWayBooking+acceptingLicenceOpensHome'`.

### On pCloudy

1. Upload the APK to your pCloudy app library.
2. Pass your credentials on the command line (or use env vars `PCLOUDY_USERNAME` / `PCLOUDY_API_KEY`), then run:

```bash
mvn test -Dexecution=pcloudy \
  -Dpcloudy.username=you@company.com -Dpcloudy.api.key=xxxxxxxx \
  -Dpcloudy.device.manufacturer=Google -Dpcloudy.platform.version=14
```

To run only one test on pCloudy (here `endToEndOneWayBooking`), add `-Dtest`:

```bash
mvn test -Dexecution=pcloudy -Dtest='PositiveTests#endToEndOneWayBooking' \
  -Dpcloudy.username=you@company.com -Dpcloudy.api.key=xxxxxxxx \
  -Dpcloudy.device.manufacturer=Google -Dpcloudy.platform.version=14
```

All settings live in `src/test/resources/config.properties`. You can override any of them with `-Dkey=value` or with an environment variable in UPPER_SNAKE_CASE.

## Test cases

| ID | Test | Method |
|---|---|---|
| TC_01 | Valid credentials open the End user Licence screen | `validLoginOpensLicenceScreen` |
| TC_02 | Accepting the licence opens Home with a welcome message | `acceptingLicenceOpensHome` |
| TC_03 | One-way search lists flights for the chosen route | `oneWaySearchShowsFlights` |
| TC_04 | Return search with several travellers shows both dates and passengers | `returnSearchShowsDatesAndTravellers` |
| TC_05 | The selected flight and fare carry over to Passenger Details | `selectedFlightCarriedToPassengerDetails` |
| TC_06 | Card details appear only after choosing Debit/Credit card | `cardFieldsAppearAfterChoosingCardType` |
| TC_07 | OTP goes to the masked mobile number and can be resent | `resendOtpGeneratesNewOtp` |
| TC_08 | **End to end:** one-way booking, login → booking confirmation | `endToEndOneWayBooking` |
| TC_09 | **End to end:** return booking with debit card, then found by its reference | `endToEndReturnBookingFoundByReference` |
| TC_10 | The pre-loaded booking is found via Single Booking Reference | `preloadedBookingFoundByReference` |
| NTC_01 | Wrong password is rejected | `wrongPasswordIsRejected` |
| NTC_02 | Refusing the licence shows a toast and blocks access | `refusingLicenceBlocksAccess` |
| NTC_03 | Same From and To city is rejected | `sameFromAndToCityIsRejected` |
| NTC_04 | Expired card is rejected | `expiredCardIsRejected` |
| NTC_05 | Wrong OTP is rejected and the booking is not confirmed | `wrongOtpIsRejected` |

The end-to-end tests use soft assertions, so a single run reports every wrong field on the confirmation screen.

### CloudAngle tests (`CloudAngleTests`)

Capability checks for app management, device interaction and DOM access through Appium. Capabilities the device or hub does not allow (for example `mobile: shell`), or that need a local file (the APK), are reported as **skipped**, not failed.

| ID | Test | Method |
|---|---|---|
| CA_01 | Install APK (needs a local APK at `app.path`) | `installApk` |
| CA_02 | Install IPA (always skipped: Android-only suite) | `installIpa` |
| CA_03 | List installed apps includes the app package | `listInstalledApps` |
| CA_04 | Clear app data returns the app to a fresh Login screen | `clearAppData` |
| CA_05 | Kill and relaunch the app | `killAndRelaunchApp` |
| CA_06 | Extract APK metadata (package, versionName, versionCode) | `extractApkMetadata` |
| CA_07 | Uninstall app (reinstalled afterwards when possible) | `uninstallApp` |
| CA_08 | Tap elements: a normal tap shows the "Normal click pressed" toast | `tapElements` |
| CA_09 | Form fill / text input | `formFillTextInput` |
| CA_10 | Long press shows the "Long pressed successfully" toast | `longPress` |
| CA_11 | Drag and drop: dropping the plane inside the box succeeds | `dragAndDropInsideBoxSucceeds` |
| NCA_11 | Drag and drop: dropping the plane outside the box fails | `dragAndDropOutsideBoxFails` |
| CA_12 | Scroll (X and Y) | `scroll` |
| CA_13 | Back / navigation | `backNavigation` |
| CA_14 | Hide keyboard | `hideKeyboard` |
| CA_15 | Page source (XML tree) | `pageSource` |
| CA_16 | Screenshot | `screenshot` |
| CA_17 | Element bounds fit the screen | `elementBounds` |
| CA_18 | Current activity / package | `currentActivityAndPackage` |
| CA_19 | Press Home, scroll up to open the app drawer, then return to the app | `homeThenScrollUp` |

`CA_01` and `CA_07` need the APK at `app.path`; `CA_03` and `CA_06` need `mobile: shell` to be allowed on the device or hub.

## Project layout

```
src/test/java/com/pcloudy/appium/
├── base/BaseTest.java           session per test, screenshot on failure, shared journeys
├── config/Config.java           config.properties + -D / env overrides
├── driver/DriverFactory.java    local Appium or pCloudy session (UiAutomator2Options)
├── data/TestData.java           credentials, cities, passenger, card and dates
├── pages/                       one page object per screen
│   ├── BasePage.java            waits, typing, scrolling, dropdowns, toasts
│   ├── LoginPage, LicensePage, HomePage
│   ├── BookFlightPage           includes the DatePicker helper
│   ├── FlightResultsPage, PassengerDetailsPage
│   ├── PaymentPage, OtpPage
│   ├── BookingConfirmationPage  also used for "Booking Details"
│   └── FindBookingPage
└── tests/
    ├── PositiveTests.java       TC_01 – TC_10
    ├── NegativeTests.java       NTC_01 – NTC_05
    └── CloudAngleTests.java     CA_01 – CA_19, NCA_11
```

## Run from Jenkins

The `Jenkinsfile` turns every test class and every test into a checkbox. In **Build with Parameters**, tick what you want and click **Build**.

- `RUN_ALL_POSITIVE` / `RUN_ALL_NEGATIVE` / `RUN_ALL_CLOUDANGLE` run a whole class.
- `TC_08_endToEndOneWayBooking`, `NTC_01_wrongPasswordIsRejected`, … run single tests. You can mix them with the class boxes.
- `EXECUTION` picks `pcloudy` or `local`. The optional fields override the device for that run.

One-time setup:

1. **Manage Jenkins → Credentials → System → Global → Add Credentials**: kind *Secret text*, ID `pcloudy-api-key`, secret = your pCloudy API key.
2. **New Item → Pipeline**, then under *Pipeline* choose *Pipeline script from SCM* → Git, enter this repo's URL, branch `*/main` and script path `Jenkinsfile`.
3. Click **Build Now** once. That run only loads the checkboxes and stops with "Nothing ticked". After it, the button becomes **Build with Parameters**.

Results appear under *Test Result* on each build, with screenshots and Surefire reports under *Build Artifacts*. Scheduling is off. To turn it on, uncomment the `pipelineTriggers` line in the `Jenkinsfile`.

## Reports

- TestNG / Surefire reports: `target/surefire-reports/` (open `index.html`)
- Screenshots (end of every test, failures prefixed with the test name): `screenshot/`
