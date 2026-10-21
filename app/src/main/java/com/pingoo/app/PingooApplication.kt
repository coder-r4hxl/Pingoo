package com.pingoo.app

import android.app.Application
import org.osmdroid.config.Configuration

class PingooApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize OSMDroid user agent configuration for tile usage attribution compliance
        Configuration.getInstance().userAgentValue = packageName
    }
}
