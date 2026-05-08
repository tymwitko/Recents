package com.tymwitko.recents.settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.navigation.compose.rememberNavController
import com.tymwitko.recents.R
import com.tymwitko.recents.common.DONATION_URL
import com.tymwitko.recents.common.ui.compost.RecentAppsTheme
import com.tymwitko.recents.recentapps.RecentAppsActivity
import com.tymwitko.recents.settings.menu.SettingsMenuViewData
import com.tymwitko.recents.settings.navi.NavigationItem
import com.tymwitko.recents.settings.navi.SettingsNavHost

class SettingsActivity: AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      RecentAppsTheme {
        SettingsNavHost(
          navController = rememberNavController(),
          lifecycleOwner = this,
          thisPackageName = this.packageName,
          settingsList = listOf(
            SettingsMenuViewData(
              resources.getString(R.string.setting_item_ui),
              ResourcesCompat.getDrawable(
                resources,
                R.drawable.settings,
                theme
              )!!.toBitmap().asImageBitmap(),
              NavigationItem.Ui.route
            ),
            SettingsMenuViewData(
              resources.getString(R.string.setting_item_whitelist),
              ResourcesCompat.getDrawable(
                resources,
                R.drawable.checkbox,
                theme
              )!!.toBitmap().asImageBitmap(),
              NavigationItem.Whitelist.route
            ),
            SettingsMenuViewData(
              resources.getString(R.string.setting_item_donate),
              ResourcesCompat.getDrawable(
                resources,
                R.drawable.donate,
                theme
              )!!.toBitmap().asImageBitmap(),
              NavigationItem.Donate.route
            )
          ),
          launchDonateLink = {
            val browserIntent = Intent(Intent.ACTION_VIEW, DONATION_URL.toUri())
            startActivity(browserIntent)
          },
          exitSettings = {
            startActivity(Intent(this, RecentAppsActivity::class.java))
          }
        )
      }
    }
  }
}

