package com.xtreme.iptv.utils

import android.content.Context
import android.content.SharedPreferences

object Prefs {
    private lateinit var sp: SharedPreferences

    fun init(ctx: Context) {
        sp = ctx.getSharedPreferences("xtreme_prefs", Context.MODE_PRIVATE)
    }

    var host: String
        get() = sp.getString("host", "") ?: ""
        set(v) = sp.edit().putString("host", v).apply()

    var user: String
        get() = sp.getString("user", "") ?: ""
        set(v) = sp.edit().putString("user", v).apply()

    var pass: String
        get() = sp.getString("pass", "") ?: ""
        set(v) = sp.edit().putString("pass", v).apply()

    var isLoggedIn: Boolean
        get() = sp.getBoolean("logged", false)
        set(v) = sp.edit().putBoolean("logged", v).apply()

    fun logout() = sp.edit().clear().apply()
}
