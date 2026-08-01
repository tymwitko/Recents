package com.tymwitko.recents.settings.menu

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tymwitko.recents.BuildConfig
import com.tymwitko.recents.recentapps.RecentAppsActivity
import com.tymwitko.recents.settings.whitelist.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsMenuScreen(
  modifier: Modifier = Modifier,
  navController: NavHostController,
  entryNames: List<SettingsMenuViewData>,
  viewModel: SettingsViewModel = koinViewModel()
) {
  val context = LocalContext.current
  BackHandler {
    context.startActivity(Intent(context, RecentAppsActivity::class.java))
  }
  Column(
    modifier = modifier
      .statusBarsPadding()
      .navigationBarsPadding()
      .fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .weight(1f)
    ) {
      SettingsList(
        navController = navController,
        entryNames = entryNames,
        fontSize = viewModel.getFontSize()
      )
    }
    Text(
      text = "v${BuildConfig.VERSION_NAME}",
      color = MaterialTheme.colorScheme.onBackground,
      fontSize = 12.sp
    )
  }
}
