package com.tymwitko.recents.overlay

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.tymwitko.recents.recentapps.RecentAppsActivity

class LaunchRouterActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val nextIntent = if (OverlayModePrefs.isEnabled(this)) {
            Intent(this, OverlayActivity::class.java).apply {
                action = OverlayService.ACTION_SHOW_OVERLAY
            }
        } else {
            Intent(this, RecentAppsActivity::class.java)
        }

        nextIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(nextIntent)

        finish()
        overridePendingTransition(0, 0)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }
}
