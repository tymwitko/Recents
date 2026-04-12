package com.tymwitko.recents.settings.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.tymwitko.recents.settings.navi.NavigationItem

@Composable
fun UiSettingsScreen(navController: NavHostController) {
  BackHandler {
    navController.navigate(NavigationItem.Menu.route)
  }
}