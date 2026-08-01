package com.tymwitko.recents.settings.navi

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tymwitko.recents.common.DONATION_URL
import com.tymwitko.recents.common.REPORT_ISSUE_URL
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
  settingsList: List<SettingsMenuViewData>
) {
  val context = LocalContext.current
  fun handleUrl(url: String) {
    val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri())
    context.startActivity(browserIntent)
  }

  NavHost(
    modifier = modifier,
    navController = navController,
    startDestination = startDestination
  ) {
    composable(NavigationItem.Whitelist.route) {
      WhitelistSettingsScreen(
        navController = navController
      )
    }
    composable(NavigationItem.Ui.route) {
      UiSettingsScreen(navController)
    }
    composable(NavigationItem.Menu.route) {
      SettingsMenuScreen(
        navController = navController,
        entryNames = settingsList
      )
    }
    composable(NavigationItem.Advanced.route) {
      AdvancedSettingsScreen(navController)
    }
    composable(NavigationItem.Pinned.route) {
      PinnedSettingsScreen(
        navController = navController
      )
    }
    composable(NavigationItem.Donate.route) {
      LaunchedEffect(Unit) {
        handleUrl(DONATION_URL)
      }
      SettingsMenuScreen(
        navController = navController,
        entryNames = settingsList
      )
    }
    composable(NavigationItem.ReportIssue.route) {
      LaunchedEffect(Unit) {
        handleUrl(REPORT_ISSUE_URL)
      }
      SettingsMenuScreen(
        navController = navController,
        entryNames = settingsList
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
  PINNED,
  ISSUE
}

sealed class NavigationItem(val route: String) {
  object Whitelist : NavigationItem(Screen.WHITELIST.name)
  object Ui : NavigationItem(Screen.UI.name)
  object Menu : NavigationItem(Screen.MENU.name)
  object Donate : NavigationItem(Screen.DONATE.name)
  object Advanced : NavigationItem(Screen.ADVANCED.name)
  object Pinned : NavigationItem(Screen.PINNED.name)
  object ReportIssue : NavigationItem(Screen.ISSUE.name)
}
