package com.tymwitko.recents.overlay

import android.content.Context

object OverlayModePrefs {

    private const val PREFS_NAME = "overlay_mode_prefs"
    private const val KEY_OVERLAY_MODE_ENABLED = "overlay_mode_enabled"
    private const val KEY_OVERLAY_PERMISSION_PROMPTED = "overlay_permission_prompted"

    const val EXTRA_FORCE_FULL_APP = "force_full_app"

    fun isEnabled(context: Context): Boolean {
        return context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_OVERLAY_MODE_ENABLED, false)
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_OVERLAY_MODE_ENABLED, enabled)
            .apply()
    }

    fun wasOverlayPermissionPrompted(context: Context): Boolean {
        return context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_OVERLAY_PERMISSION_PROMPTED, false)
    }

    fun setOverlayPermissionPrompted(context: Context, prompted: Boolean) {
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_OVERLAY_PERMISSION_PROMPTED, prompted)
            .apply()
    }
}
