package com.tymwitko.recents.settings.menu

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.navigation.NavHostController

@Composable
fun SettingsList(
  modifier: Modifier = Modifier,
  navController: NavHostController,
  entryNames: List<SettingsMenuViewData>,
  fontSize: TextUnit
) {
  LazyColumn(modifier = modifier) {
    items(items = entryNames.toList()) {
      SettingsMenuItem(
        it.name,
        it.icon,
        it.route,
        navController,
        fontSize
      )
    }
  }
}
