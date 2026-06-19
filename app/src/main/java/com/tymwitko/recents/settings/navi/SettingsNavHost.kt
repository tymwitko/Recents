package com.tymwitko.recents.settings.navi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tymwitko.recents.common.DONATE_EFFECT_KEY
import com.tymwitko.recents.settings.advanced.AdvancedSettingsScreen
import com.tymwitko.recents.settings.menu.SettingsMenuScreen
import com.tymwitko.recents.settings.menu.SettingsMenuViewData
import com.tymwitko.recents.settings.pinned.PinnedSettingsScreen
import com.tymwitko.recents.settings.ui.UiSettingsScreen
import com.tymwitko.recents.settings.whitelist.WhitelistSettingsScreen

@Composable
fun SettingsNavHost(
  modifier: Modifier = Modifier,
  navController: NavHostController,
  startDestination: String = NavigationItem.Menu.route,
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
        navController = navController
      )
    }
    composable(NavigationItem.Ui.route) {
      UiSettingsScreen(navController)
    }
    composable(NavigationItem.Menu.route) {
      SettingsMenuScreen(
        navController = navController,
        entryNames = settingsList,
        exitSettings = exitSettings
      )
    }
    composable(NavigationItem.Advanced.route) {
      AdvancedSettingsScreen(navController)
    }
    composable(NavigationItem.Pinned.route) {
      PinnedSettingsScreen(
        navController = navController,
        thisPackageName = thisPackageName
      )
    }
    composable(NavigationItem.Donate.route) {
      LaunchedEffect(keys = arrayOf(DONATE_EFFECT_KEY)) {
        launchDonateLink()
      }
      SettingsMenuScreen(
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
  DONATE,
  ADVANCED,
  PINNED
}

sealed class NavigationItem(val route: String) {
  object Whitelist : NavigationItem(Screen.WHITELIST.name)
  object Ui : NavigationItem(Screen.UI.name)
  object Menu : NavigationItem(Screen.MENU.name)
  object Donate : NavigationItem(Screen.DONATE.name)
  object Advanced : NavigationItem(Screen.ADVANCED.name)
  object Pinned : NavigationItem(Screen.PINNED.name)
}