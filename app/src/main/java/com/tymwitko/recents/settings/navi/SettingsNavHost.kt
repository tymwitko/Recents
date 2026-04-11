package com.tymwitko.recents.settings.navi

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
  defaultIconForApp: ImageBitmap?,
  settingsList: List<SettingsMenuViewData>
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
        defaultIcon = defaultIconForApp
      )
    }
    composable(NavigationItem.Ui.route) {
      UiSettingsScreen()
    }
    composable(NavigationItem.Menu.route) {
      SettingsMenu(
        navController = navController,
        entryNames = settingsList
      )
    }
  }
}

enum class Screen {
  WHITELIST,
  UI,
  MENU
}

sealed class NavigationItem(val route: String) {
  object Whitelist : NavigationItem(Screen.WHITELIST.name)
  object Ui : NavigationItem(Screen.UI.name)
  object Menu : NavigationItem(Screen.MENU.name)
}