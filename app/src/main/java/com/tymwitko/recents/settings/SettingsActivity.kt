package com.tymwitko.recents.settings

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.compose.rememberNavController
import com.tymwitko.recents.R
import com.tymwitko.recents.common.ui.compost.RecentAppsTheme
import com.tymwitko.recents.common.ui.toImageBitmap
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
          settingsList = listOf(
            SettingsMenuViewData(
              resources.getString(R.string.setting_item_ui),
              ResourcesCompat.getDrawable(
                resources,
                R.drawable.ui_settings,
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
              stringResource(R.string.pinned_app_settings),
              painterResource(R.drawable.pin).toImageBitmap(
                LocalDensity.current, LocalLayoutDirection.current
              ),
              NavigationItem.Pinned.route
            ),
            SettingsMenuViewData(
              resources.getString(R.string.setting_item_advanced),
              ResourcesCompat.getDrawable(
                resources,
                R.drawable.advanced_settings_wrench,
                theme
              )!!.toBitmap().asImageBitmap(),
              NavigationItem.Advanced.route
            ),
            SettingsMenuViewData(
              resources.getString(R.string.setting_item_donate),
              ResourcesCompat.getDrawable(
                resources,
                R.drawable.donate,
                theme
              )!!.toBitmap().asImageBitmap(),
              NavigationItem.Donate.route
            ),
            SettingsMenuViewData(
              resources.getString(R.string.setting_item_report),
              ResourcesCompat.getDrawable(
                resources,
                R.drawable.bug_report,
                theme
              )!!.toBitmap().asImageBitmap(),
              NavigationItem.ReportIssue.route
            ),
            SettingsMenuViewData(
              resources.getString(R.string.setting_item_logs),
              ResourcesCompat.getDrawable(
                resources,
                R.drawable.download,
                theme
              )!!.toBitmap().asImageBitmap(),
              NavigationItem.ReportIssue.route
            )
          )
        )
      }
    }
  }
}
