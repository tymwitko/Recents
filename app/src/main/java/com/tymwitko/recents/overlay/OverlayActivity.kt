package com.tymwitko.recents.overlay

import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.widget.Toast

class OverlayActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openOverlayOrPermissions()
    }

    override fun onResume() {
        super.onResume()

        if (
            hasUsageStatsPermission() &&
            OverlayPermissionHelper.canDrawOverlays(this)
        ) {
            OverlayModePrefs.setOverlayPermissionPrompted(this, false)
            OverlayService.show(this)
            finish()
            overridePendingTransition(0, 0)
        }
    }

    private fun openOverlayOrPermissions() {
        when {
            !hasUsageStatsPermission() -> {
                Toast.makeText(
                    this,
                    "Enable Usage Access for Recents, then open Overlay Mode again.",
                    Toast.LENGTH_LONG
                ).show()

                startActivity(
                    Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )

                finish()
                overridePendingTransition(0, 0)
            }

            OverlayPermissionHelper.canDrawOverlays(this) -> {
                OverlayModePrefs.setOverlayPermissionPrompted(this, false)
                OverlayService.show(this)
                finish()
                overridePendingTransition(0, 0)
            }

            !OverlayModePrefs.wasOverlayPermissionPrompted(this) -> {
                OverlayModePrefs.setOverlayPermissionPrompted(this, true)

                Toast.makeText(
                    this,
                    "Allow Recents to draw over other apps, then open Overlay Mode again.",
                    Toast.LENGTH_LONG
                ).show()

                OverlayPermissionHelper.openOverlayPermissionSettings(this)

                finish()
                overridePendingTransition(0, 0)
            }

            else -> {
                OverlayService.show(this)
                finish()
                overridePendingTransition(0, 0)
            }
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager

        @Suppress("DEPRECATION")
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            packageName
        )

        return mode == AppOpsManager.MODE_ALLOWED
    }
}
