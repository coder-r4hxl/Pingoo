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

    val historyTitle: String,
    val noHistoryYet: String,

    val settingsTitle: String,
    val languageSelection: String,
    val selectLanguage: String,
    val vibrationSetting: String,
    val defaultRadiusSetting: String,
    val appVersion: String,
    val phaseNotice: String
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
        statusIdle = "Monitoring Idle (Phase 1 Foundation)",

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
        liveGpsStatus = "GPS Status: Waiting for active service (Phase 2 feature)",
        distanceToDestination = "Distance: --",
        startMonitoring = "Start GPS Monitoring",
        stopMonitoring = "Stop GPS Monitoring",

        historyTitle = "Journey History",
        noHistoryYet = "No previous arrival history logged.",

        settingsTitle = "Settings & Preferences",
        languageSelection = "Language / भाषा",
        selectLanguage = "Select Language",
        vibrationSetting = "Vibration Alert",
        defaultRadiusSetting = "Default Arrival Radius (Meters)",
        appVersion = "Pingoo v1.0.0 (Phase 1)",
        phaseNotice = "Phase 1 Foundation Active. GPS tracking and background audio alarms will be enabled in Phase 2."
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
        statusIdle = "निगरानी निष्क्रिय (चरण 1 आधार)",

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
        liveGpsStatus = "जीपीएस स्थिति: सक्रिय सेवा की प्रतीक्षा है (चरण 2 सुविधा)",
        distanceToDestination = "दूरी: --",
        startMonitoring = "जीपीएस निगरानी शुरू करें",
        stopMonitoring = "जीपीएस निगरानी रोकें",

        historyTitle = "यात्रा इतिहास",
        noHistoryYet = "कोई पिछला आगमन इतिहास नहीं दर्ज है।",

        settingsTitle = "सेटिंग्स और प्राथमिकताएं",
        languageSelection = "भाषा / Language",
        selectLanguage = "भाषा चुनें",
        vibrationSetting = "कंपन अलर्ट (Vibration)",
        defaultRadiusSetting = "डिफ़ॉल्ट पहुँच त्रिज्या (मीटर)",
        appVersion = "पिंगू v1.0.0 (चरण 1)",
        phaseNotice = "चरण 1 आधार सक्रिय है। जीपीएस ट्रैकिंग और बैकग्राउंड ऑडियो अलार्म चरण 2 में सक्षम किए जाएंगे।"
    )

    fun get(language: Language): AppStrings = when (language) {
        Language.ENGLISH -> english
        Language.HINDI -> hindi
    }
}
