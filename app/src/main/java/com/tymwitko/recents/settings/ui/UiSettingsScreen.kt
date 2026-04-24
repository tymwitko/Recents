package com.tymwitko.recents.settings.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tymwitko.recents.R
import com.tymwitko.recents.common.ui.toImageBitmap
import com.tymwitko.recents.recentapps.RecentAppsItem
import com.tymwitko.recents.settings.navi.NavigationItem
import com.tymwitko.recents.settings.whitelist.ui.WhitelistItem
import org.koin.androidx.compose.koinViewModel

@Composable
fun UiSettingsScreen(
  navController: NavHostController,
  viewModel: UiSettingsViewModel = koinViewModel()
) {
  BackHandler {
    navController.navigate(NavigationItem.Menu.route)
  }
  var sliderPosition by rememberSaveable { mutableFloatStateOf(viewModel.getFontSize().value) }
  Column(
    modifier = Modifier
      .navigationBarsPadding()
      .statusBarsPadding()
      .padding(vertical = 24.dp)
  ) {
    SizeSlider(sliderPosition) {
      sliderPosition = it
      viewModel.saveFontSize(it)
    }
    RecentAppsItem(
      name = "Recents",
      packageName = "com.tymwitko.recents",
      icon = painterResource(R.drawable.app_icon).toImageBitmap(
        LocalDensity.current,
        LocalLayoutDirection.current
      ),
      launchApp = {},
      killApp = {},
      showQuickSettings = { _, _, _, _ -> },
      hasPrivileges = true,
      fontSize = sliderPosition.sp
    )
    WhitelistItem(
      name = "Recents",
      packageName = "com.tymwitko.recents",
      icon = painterResource(R.drawable.app_icon).toImageBitmap(
        LocalDensity.current,
        LocalLayoutDirection.current
      ),
      showKillCheck = true,
      fontSize = sliderPosition.sp,
      whitelistLaunch = { _, _ -> },
      whitelistKill = { _, _ ->},
      whitelistShow = { _, _ -> },
      settings = null,
      lifecycleOwner = null,
    )
  }
}
