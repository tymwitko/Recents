package com.tymwitko.recents.settings

import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.rememberNavController
import com.tymwitko.recents.R
import com.tymwitko.recents.common.ui.compost.RecentAppsTheme
import com.tymwitko.recents.settings.menu.SettingsMenuViewData
import com.tymwitko.recents.settings.navi.NavigationItem
import com.tymwitko.recents.settings.navi.SettingsNavHost
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity: AppCompatActivity() {

  private val viewModel by viewModel<SettingsViewModel>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val createLogFileLauncher =
      registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri: Uri? ->
        if (uri != null) {
          viewModel.saveLogsToUri(contentResolver, uri)
        }
      }

    enableEdgeToEdge()
    setContent {
      RecentAppsTheme {
        SettingsNavHost(
          navController = rememberNavController(),
          promptLauncher = createLogFileLauncher,
          settingsList = listOf(
            SettingsMenuViewData(
              resources.getString(R.string.setting_item_ui),
              painterResource(R.drawable.ui_settings,),
              NavigationItem.Ui.route
            ),
            SettingsMenuViewData(
              resources.getString(R.string.setting_item_whitelist),
              painterResource(R.drawable.checkbox),
              NavigationItem.Whitelist.route
            ),
            SettingsMenuViewData(
              stringResource(R.string.pinned_app_settings),
              painterResource(R.drawable.pin),
              NavigationItem.Pinned.route
            ),
            SettingsMenuViewData(
              resources.getString(R.string.setting_item_advanced),
              painterResource(R.drawable.advanced_settings_wrench),
              NavigationItem.Advanced.route
            ),
            SettingsMenuViewData(
              resources.getString(R.string.setting_item_donate),
              painterResource(R.drawable.donate),
              NavigationItem.Donate.route
            ),
            SettingsMenuViewData(
              resources.getString(R.string.setting_item_report),
              painterResource(R.drawable.bug_report),
              NavigationItem.ReportIssue.route
            ),
            SettingsMenuViewData(
              resources.getString(R.string.setting_item_logs),
              painterResource(R.drawable.download),
              NavigationItem.DownloadLogs.route
            )
          )
        )
      }
    }
  }
}
