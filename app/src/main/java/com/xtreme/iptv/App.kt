package com.xtreme.iptv

import android.app.Application
import com.xtreme.iptv.utils.Prefs

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Prefs.init(this)
    }
}
