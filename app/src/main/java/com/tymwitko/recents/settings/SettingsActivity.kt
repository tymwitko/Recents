package com.tymwitko.recents.settings

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.compose.rememberNavController
import com.tymwitko.recents.R
import com.tymwitko.recents.common.ui.compost.RecentAppsTheme
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
          defaultIconForApp = ResourcesCompat.getDrawable(
            resources,
            android.R.drawable.ic_menu_gallery,
            null
          )
            ?.toBitmap()?.asImageBitmap(),
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
              null
            )
          )
        )
      }
    }
  }
}

