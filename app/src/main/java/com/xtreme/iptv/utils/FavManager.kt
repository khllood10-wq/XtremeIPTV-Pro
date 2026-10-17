package com.xtreme.iptv.utils

import android.content.Context

object FavManager {
    private const val PREF_NAME = "fav_prefs"
    private const val KEY_FAVS = "fav_ids"

    fun getFavs(ctx: Context): Set<String> {
        val prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_FAVS, emptySet()) ?: emptySet()
    }

    fun toggleFav(ctx: Context, streamId: Int): Boolean {
        val prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val favs = getFavs(ctx).toMutableSet()
        val idStr = streamId.toString()
        val isAdded = if (favs.contains(idStr)) {
            favs.remove(idStr)
            false
        } else {
            favs.add(idStr)
            true
        }
        prefs.edit().putStringSet(KEY_FAVS, favs).apply()
        return isAdded
    }

    fun isFav(ctx: Context, streamId: Int): Boolean {
        return getFavs(ctx).contains(streamId.toString())
    }
}
