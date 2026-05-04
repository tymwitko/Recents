package com.tymwitko.recents.settings.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tymwitko.recents.R
import com.tymwitko.recents.common.dataclasses.App
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
  val defaultIconSize = dimensionResource(R.dimen.icon_dimension).value.toInt()
  var fontSliderPosition by rememberSaveable { mutableFloatStateOf(viewModel.getFontSize().value) }
  var iconSliderPosition by rememberSaveable {
    mutableFloatStateOf(viewModel.getIconSize(defaultIconSize).value)
  }
  Column(
    modifier = Modifier
      .navigationBarsPadding()
      .statusBarsPadding()
      .padding(vertical = 24.dp)
  ) {
    SizeSlider(
      sliderPosition = fontSliderPosition,
      label = stringResource(R.string.set_font_size),
      valueRange = 3F..24F
    ) {
      fontSliderPosition = it
      viewModel.saveFontSize(it)
    }
    SizeSlider(
      sliderPosition = iconSliderPosition,
      label = stringResource(R.string.set_icon_size),
      valueRange = 50F..150F
    ) {
      iconSliderPosition = it
      viewModel.saveIconSize(it)
    }
    Text(
      modifier = Modifier.padding(24.dp),
      text = stringResource(R.string.preview),
      color = MaterialTheme.colorScheme.onBackground
    )
    RecentAppsItem(
      app = App(
        name = "Recents",
        packageName = "com.tymwitko.recents",
        icon = painterResource(R.drawable.app_icon).toImageBitmap(
          LocalDensity.current,
          LocalLayoutDirection.current
        )
      ),
      launchApp = {},
      killApp = {},
      showQuickSettings = { _, _, _, _ -> },
      hasPrivileges = viewModel.hasPrivileges(),
      fontSize = fontSliderPosition.sp,
      iconSize = iconSliderPosition.dp
    )
    WhitelistItem(
      name = "Recents",
      packageName = "com.tymwitko.recents",
      icon = painterResource(R.drawable.app_icon).toImageBitmap(
        LocalDensity.current,
        LocalLayoutDirection.current
      ),
      showKillCheck = viewModel.hasPrivileges(),
      fontSize = fontSliderPosition.sp,
      iconSize = iconSliderPosition.dp,
      whitelistLaunch = { _, _ -> },
      whitelistKill = { _, _ ->},
      whitelistShow = { _, _ -> },
      settings = null,
      lifecycleOwner = null,
    )
  }
}
