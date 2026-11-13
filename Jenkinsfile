// Jenkins pipeline for Appium-Test-Project.
// Open "Build with Parameters", tick the classes and/or single tests you want, then click Build.
// The first build after creating the job only registers these parameters (it stops with "nothing ticked").

// Every test that can be ticked: [id, class, method, description].
// To add a test, add one line here.
def TESTS = [
    ['TC_01',  'PositiveTests',   'validLoginOpensLicenceScreen',          'Valid credentials open the End user Licence screen'],
    ['TC_02',  'PositiveTests',   'acceptingLicenceOpensHome',             'Accepting the licence opens Home with a welcome message'],
    ['TC_03',  'PositiveTests',   'oneWaySearchShowsFlights',              'One-way search lists flights for the chosen route'],
    ['TC_04',  'PositiveTests',   'returnSearchShowsDatesAndTravellers',   'Return search with several travellers shows both dates and passengers'],
    ['TC_05',  'PositiveTests',   'selectedFlightCarriedToPassengerDetails', 'The selected flight and fare are carried to Passenger Details'],
    ['TC_06',  'PositiveTests',   'cardFieldsAppearAfterChoosingCardType', 'Card details appear only after choosing Debit/Credit card'],
    ['TC_07',  'PositiveTests',   'resendOtpGeneratesNewOtp',              'OTP is sent to the masked mobile number and can be resent'],
    ['TC_08',  'PositiveTests',   'endToEndOneWayBooking',                 'End-to-end one-way booking from login to booking confirmation'],
    ['TC_09',  'PositiveTests',   'endToEndReturnBookingFoundByReference', 'End-to-end return booking with debit card, found by its reference'],
    ['TC_10',  'PositiveTests',   'preloadedBookingFoundByReference',      'The pre-loaded booking is found via Single Booking Reference'],

    ['NTC_01', 'NegativeTests',   'wrongPasswordIsRejected',               'Wrong password is rejected on the Login screen'],
    ['NTC_02', 'NegativeTests',   'refusingLicenceBlocksAccess',           'Refusing the licence shows a toast and blocks access to Home'],
    ['NTC_03', 'NegativeTests',   'sameFromAndToCityIsRejected',           'Same From and To city shows an error'],
    ['NTC_04', 'NegativeTests',   'expiredCardIsRejected',                 'An expired card is rejected on the Payment screen'],
    ['NTC_05', 'NegativeTests',   'wrongOtpIsRejected',                    'A wrong OTP is rejected and the booking is not confirmed'],

    ['CA_01',  'CloudAngleTests', 'installApk',                            'Install APK (needs the APK at app.path)'],
    ['CA_02',  'CloudAngleTests', 'installIpa',                            'Install IPA (always skipped: Android-only)'],
    ['CA_03',  'CloudAngleTests', 'listInstalledApps',                     'List installed apps (needs mobile: shell)'],
    ['CA_04',  'CloudAngleTests', 'clearAppData',                          'Clear app data returns the app to a fresh Login screen'],
    ['CA_05',  'CloudAngleTests', 'killAndRelaunchApp',                    'Kill and relaunch the app'],
    ['CA_06',  'CloudAngleTests', 'extractApkMetadata',                    'Extract APK metadata (needs mobile: shell)'],
    ['CA_07',  'CloudAngleTests', 'uninstallApp',                          'Uninstall app (needs the APK at app.path)'],
    ['CA_08',  'CloudAngleTests', 'tapElements',                           'Tap elements'],
    ['CA_09',  'CloudAngleTests', 'formFillTextInput',                     'Form fill / text input'],
    ['CA_10',  'CloudAngleTests', 'longPress',                             'Long press'],
    ['CA_11',  'CloudAngleTests', 'dragAndDropInsideBoxSucceeds',          'Drag and drop inside the box succeeds'],
    ['NCA_11', 'CloudAngleTests', 'dragAndDropOutsideBoxFails',            'Drag and drop outside the box fails'],
    ['CA_12',  'CloudAngleTests', 'scroll',                                'Scroll (X and Y)'],
    ['CA_13',  'CloudAngleTests', 'backNavigation',                        'Back / navigation'],
    ['CA_14',  'CloudAngleTests', 'hideKeyboard',                          'Hide keyboard'],
    ['CA_15',  'CloudAngleTests', 'pageSource',                            'Page source (XML tree)'],
    ['CA_16',  'CloudAngleTests', 'screenshot',                            'Screenshot'],
    ['CA_17',  'CloudAngleTests', 'elementBounds',                         'Element bounds'],
    ['CA_18',  'CloudAngleTests', 'currentActivityAndPackage',             'Current activity / package'],
    ['CA_19',  'CloudAngleTests', 'homeThenScrollUp',                      'Press Home, open the app drawer, return to the app'],
]

// Whole-class checkboxes: [parameter name, class, description].
def CLASSES = [
    ['RUN_ALL_POSITIVE',   'PositiveTests',   'Run ALL Positive tests (TC_01 - TC_10)'],
    ['RUN_ALL_NEGATIVE',   'NegativeTests',   'Run ALL Negative tests (NTC_01 - NTC_05)'],
    ['RUN_ALL_CLOUDANGLE', 'CloudAngleTests', 'Run ALL CloudAngle tests (CA_01 - CA_19, NCA_11)'],
]

// Builds the Maven -Dtest value from the ticked boxes, e.g. "NegativeTests,PositiveTests#a+b".
@NonCPS
def buildTestSelection(Map p, List tests, List classes) {
    def parts = []
    classes.each { c ->
        if (p[c[0]]) {
            parts << c[1]
            return
        }
        def methods = tests.findAll { it[1] == c[1] && p["${it[0]}_${it[2]}".toString()] }.collect { it[2] }
        if (methods) {
            parts << "${c[1]}#${methods.join('+')}"
        }
    }
    return parts.join(',')
}

def paramList = [
    choice(name: 'EXECUTION', choices: ['pcloudy', 'local'],
           description: 'pcloudy = pCloudy cloud device, local = Appium + emulator/device on the Jenkins machine'),
    string(name: 'PCLOUDY_MANUFACTURER', defaultValue: '',
           description: 'Optional, e.g. Samsung, Google. Blank = value in config.properties'),
    string(name: 'PCLOUDY_PLATFORM_VERSION', defaultValue: '',
           description: 'Optional Android version, e.g. 14. Blank = value in config.properties'),
    string(name: 'DEVICE_UDID', defaultValue: '',
           description: 'Local runs only: adb serial, e.g. emulator-5554. Blank = the only connected device'),
]
CLASSES.each { c -> paramList << booleanParam(name: c[0], defaultValue: false, description: c[2]) }
TESTS.each { t -> paramList << booleanParam(name: "${t[0]}_${t[2]}".toString(), defaultValue: false, description: "${t[0]}: ${t[3]}".toString()) }

properties([
    parameters(paramList),
    disableConcurrentBuilds(),
    buildDiscarder(logRotator(numToKeepStr: '20')),
    // Scheduling is off for now. To run every night at ~2 AM, uncomment the line below
    // (scheduled runs use the parameter defaults, so also set the boxes you want to default: true).
    // pipelineTriggers([cron('H 2 * * *')]),
])

pipeline {
    agent any

    stages {
        stage('Select tests') {
            steps {
                script {
                    env.TEST_SELECTION = buildTestSelection(params, TESTS, CLASSES)
                    if (!env.TEST_SELECTION) {
                        error('Nothing ticked. Open "Build with Parameters" and tick at least one class or test.')
                    }
                    echo "Execution : ${params.EXECUTION}"
                    echo "Tests     : ${env.TEST_SELECTION}"
                }
            }
        }

        stage('Run tests') {
            steps {
                script {
                    def opts = ["-Dexecution=${params.EXECUTION}"]
                    if (params.PCLOUDY_MANUFACTURER?.trim()) {
                        opts << "-Dpcloudy.device.manufacturer='${params.PCLOUDY_MANUFACTURER.trim()}'"
                    }
                    if (params.PCLOUDY_PLATFORM_VERSION?.trim()) {
                        opts << "-Dpcloudy.platform.version='${params.PCLOUDY_PLATFORM_VERSION.trim()}'"
                    }
                    if (params.DEVICE_UDID?.trim()) {
                        opts << "-Ddevice.udid='${params.DEVICE_UDID.trim()}'"
                    }
                    def mvnCmd = "mvn -B test ${opts.join(' ')} -Dtest='${env.TEST_SELECTION}'"

                    if (params.EXECUTION == 'pcloudy') {
                        // The key comes from the Jenkins credential and reaches Config.java as env PCLOUDY_API_KEY.
                        withCredentials([string(credentialsId: 'pcloudy-api-key', variable: 'PCLOUDY_API_KEY')]) {
                            sh mvnCmd
                        }
                    } else {
                        sh mvnCmd
                    }
                }
            }
        }
    }

    post {
        always {
            junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
            archiveArtifacts artifacts: 'screenshot/**, target/surefire-reports/**', allowEmptyArchive: true
        }
    }
}
