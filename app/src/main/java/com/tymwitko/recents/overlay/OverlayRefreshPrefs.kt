package com.tymwitko.recents.overlay

import android.content.Context

object OverlayRefreshPrefs {

    private const val PREFS_NAME = "overlay_refresh_prefs"
    private const val KEY_REFRESH_SECONDS = "refresh_seconds"

    private const val DEFAULT_REFRESH_SECONDS = 10

    fun getRefreshSeconds(context: Context): Int {
        return context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_REFRESH_SECONDS, DEFAULT_REFRESH_SECONDS)
    }

    fun setRefreshSeconds(
        context: Context,
        seconds: Int,
    ) {
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_REFRESH_SECONDS, seconds)
            .apply()
    }
}