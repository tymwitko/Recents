package com.tymwitko.recents.settings.navi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tymwitko.recents.common.DONATE_EFFECT_KEY
import com.tymwitko.recents.settings.menu.SettingsMenu
import com.tymwitko.recents.settings.menu.SettingsMenuViewData
import com.tymwitko.recents.settings.ui.UiSettingsScreen
import com.tymwitko.recents.settings.whitelist.WhitelistSettingsScreen

@Composable
fun SettingsNavHost(
  modifier: Modifier = Modifier,
  navController: NavHostController,
  startDestination: String = NavigationItem.Menu.route,
  lifecycleOwner: LifecycleOwner,
  thisPackageName: String,
  settingsList: List<SettingsMenuViewData>,
  launchDonateLink: () -> Unit,
  exitSettings: () -> Unit
) {
  NavHost(
    modifier = modifier,
    navController = navController,
    startDestination = startDestination
  ) {
    composable(NavigationItem.Whitelist.route) {
      WhitelistSettingsScreen(
        thisPackageName = thisPackageName,
        lifecycleOwner = lifecycleOwner,
        navController = navController
      )
    }
    composable(NavigationItem.Ui.route) {
      UiSettingsScreen(navController)
    }
    composable(NavigationItem.Menu.route) {
      SettingsMenu(
        navController = navController,
        entryNames = settingsList,
        exitSettings = exitSettings
      )
    }
    composable(NavigationItem.Donate.route) {
      LaunchedEffect(keys = arrayOf(DONATE_EFFECT_KEY)) {
        launchDonateLink()
      }
      SettingsMenu(
        navController = navController,
        entryNames = settingsList,
        exitSettings = exitSettings
      )
    }
  }
}

enum class Screen {
  WHITELIST,
  UI,
  MENU,
  DONATE
}

sealed class NavigationItem(val route: String) {
  object Whitelist : NavigationItem(Screen.WHITELIST.name)
  object Ui : NavigationItem(Screen.UI.name)
  object Menu : NavigationItem(Screen.MENU.name)
  object Donate : NavigationItem(Screen.DONATE.name)
}