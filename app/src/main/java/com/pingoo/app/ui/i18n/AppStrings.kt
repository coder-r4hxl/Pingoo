package com.pingoo.app.ui.i18n

import com.pingoo.app.domain.model.Language

data class AppStrings(
    val appName: String,
    val tagline: String,
    val navHome: String,
    val navDestinations: String,
    val navJourney: String,
    val navHistory: String,
    val navSettings: String,

    val homeTitle: String,
    val activeAlarms: String,
    val noActiveAlarms: String,
    val quickAddDestination: String,
    val currentStatus: String,
    val statusIdle: String,

    val destinationsTitle: String,
    val addDestination: String,
    val destinationName: String,
    val latitude: String,
    val longitude: String,
    val arrivalRadius: String,
    val save: String,
    val cancel: String,
    val delete: String,
    val noDestinationsYet: String,

    val journeyTitle: String,
    val liveGpsStatus: String,
    val distanceToDestination: String,
    val startMonitoring: String,
    val stopMonitoring: String,
    val pauseMonitoring: String,
    val resumeMonitoring: String,
    val selectDestinationPrompt: String,
    val permissionRequiredNotice: String,
    val grantPermissions: String,
    val gpsAccuracyLabel: String,
    val arrivalRadiusLabel: String,
    val journeyStateLabel: String,

    val historyTitle: String,
    val noHistoryYet: String,

    val settingsTitle: String,
    val languageSelection: String,
    val selectLanguage: String,
    val vibrationSetting: String,
    val defaultRadiusSetting: String,
    val appVersion: String,
    val phaseNotice: String,

    val alarmArrivalHeader: String,
    val alarmArrivedMessage: String,
    val alarmSnoozeButton: String,
    val alarmDismissButton: String
)

object Translations {
    val english = AppStrings(
        appName = "Pingoo",
        tagline = "Your Journey. Your Arrival. Your Alert.",
        navHome = "Home",
        navDestinations = "Destinations",
        navJourney = "Journey",
        navHistory = "History",
        navSettings = "Settings",

        homeTitle = "Welcome to Pingoo",
        activeAlarms = "Active Location Alarms",
        noActiveAlarms = "No active alarms configured. Add a destination to get started.",
        quickAddDestination = "+ Add Destination",
        currentStatus = "Journey Status",
        statusIdle = "Monitoring Idle",

        destinationsTitle = "My Destinations",
        addDestination = "Add New Destination",
        destinationName = "Destination Name (e.g., Central Station)",
        latitude = "Latitude (-90 to 90)",
        longitude = "Longitude (-180 to 180)",
        arrivalRadius = "Arrival Radius (Meters)",
        save = "Save",
        cancel = "Cancel",
        delete = "Delete",
        noDestinationsYet = "No destinations saved yet.",

        journeyTitle = "Live Journey Tracker",
        liveGpsStatus = "GPS Status",
        distanceToDestination = "Distance Remaining",
        startMonitoring = "Start Journey Tracking",
        stopMonitoring = "Stop Journey Tracking",
        pauseMonitoring = "Pause",
        resumeMonitoring = "Resume",
        selectDestinationPrompt = "Select target destination:",
        permissionRequiredNotice = "Location permission is required to calculate live distance and trigger destination alerts in background.",
        grantPermissions = "Grant Location Permission",
        gpsAccuracyLabel = "GPS Accuracy",
        arrivalRadiusLabel = "Arrival Radius",
        journeyStateLabel = "State",

        historyTitle = "Journey History",
        noHistoryYet = "No previous arrival history logged.",

        settingsTitle = "Settings & Preferences",
        languageSelection = "Language / भाषा",
        selectLanguage = "Select Language",
        vibrationSetting = "Vibration Alert",
        defaultRadiusSetting = "Default Arrival Radius (Meters)",
        appVersion = "Pingoo v1.0.0 (Phase 3)",
        phaseNotice = "Phase 3 Alarm Engine Active. Full-screen arrival alert, ringtone, vibration, snooze and dismissal enabled.",

        alarmArrivalHeader = "ARRIVED AT DESTINATION!",
        alarmArrivedMessage = "You have reached your destination radius.",
        alarmSnoozeButton = "Snooze (5 Mins)",
        alarmDismissButton = "Dismiss Alarm"
    )

    val hindi = AppStrings(
        appName = "पिंगू (Pingoo)",
        tagline = "आपकी यात्रा। आपकी पहुँच। आपका अलर्ट।",
        navHome = "होम",
        navDestinations = "गंतव्य (Destinations)",
        navJourney = "यात्रा (Journey)",
        navHistory = "इतिहास (History)",
        navSettings = "सेटिंग्स",

        homeTitle = "पिंगू में आपका स्वागत है",
        activeAlarms = "सक्रिय स्थान अलार्म",
        noActiveAlarms = "कोई सक्रिय अलार्म सेट नहीं है। शुरू करने के लिए गंतव्य जोड़ें।",
        quickAddDestination = "+ गंतव्य जोड़ें",
        currentStatus = "यात्रा स्थिति",
        statusIdle = "निगरानी निष्क्रिय",

        destinationsTitle = "मेरे गंतव्य",
        addDestination = "नया गंतव्य जोड़ें",
        destinationName = "गंतव्य नाम (उदा. सेंट्रल स्टेशन)",
        latitude = "अक्षांश (-90 से 90)",
        longitude = "रेखांश (-180 से 180)",
        arrivalRadius = "पहुँच त्रिज्या (मीटर)",
        save = "सहेजें",
        cancel = "रद्द करें",
        delete = "हटाएं",
        noDestinationsYet = "अभी तक कोई गंतव्य सहेजा नहीं गया है।",

        journeyTitle = "लाइव यात्रा ट्रैकर",
        liveGpsStatus = "जीपीएस स्थिति",
        distanceToDestination = "शेष दूरी",
        startMonitoring = "यात्रा ट्रैकिंग शुरू करें",
        stopMonitoring = "यात्रा ट्रैकिंग रोकें",
        pauseMonitoring = "विराम (Pause)",
        resumeMonitoring = "पुनः प्रारंभ (Resume)",
        selectDestinationPrompt = "लक्ष्य गंतव्य चुनें:",
        permissionRequiredNotice = "लाइव दूरी की गणना करने और बैकग्राउंड में अलर्ट ट्रिगर करने के लिए स्थान अनुमति आवश्यक है।",
        grantPermissions = "स्थान अनुमति दें",
        gpsAccuracyLabel = "जीपीएस सटीकता",
        arrivalRadiusLabel = "पहुँच त्रिज्या",
        journeyStateLabel = "स्थिति",

        historyTitle = "यात्रा इतिहास",
        noHistoryYet = "कोई पिछला आगमन इतिहास नहीं दर्ज है।",

        settingsTitle = "सेटिंग्स और प्राथमिकताएं",
        languageSelection = "भाषा / Language",
        selectLanguage = "भाषा चुनें",
        vibrationSetting = "कंपन अलर्ट (Vibration)",
        defaultRadiusSetting = "डिफ़ॉल्ट पहुँच त्रिज्या (मीटर)",
        appVersion = "पिंगू v1.0.0 (चरण 3)",
        phaseNotice = "चरण 3 अलार्म इंजन सक्रिय है। फुल-स्क्रीन आगमन अलर्ट, रिंगटोन, कंपन, स्नूज़ और स्नूज़ बटन सक्षम हैं।",

        alarmArrivalHeader = "गंतव्य पर पहुँच गए!",
        alarmArrivedMessage = "आप अपने लक्ष्य गंतव्य की त्रिज्या में पहुँच चुके हैं।",
        alarmSnoozeButton = "स्नूज़ करें (5 मिनट)",
        alarmDismissButton = "अलार्म बंद करें"
    )

    fun get(language: Language): AppStrings = when (language) {
        Language.ENGLISH -> english
        Language.HINDI -> hindi
    }
}
