package com.tymwitko.recents.recentapps

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.tymwitko.recents.common.dataclasses.App

@Composable
fun RecentAppsList(
  modifier: Modifier = Modifier,
  appList: List<App>,
  hasPrivileges: Boolean,
  isSwipeToKill: Boolean,
  iconSize: Dp,
  fontSize: TextUnit,
  launchApp: (App) -> Unit,
  showQuickSettings: (App, Int, Int) -> Unit,
) {
  LazyColumn(modifier = modifier) {
    items(items = appList, key = { it.packageName }) {
      RecentAppsItem(
        it,
        hasPrivileges,
        isSwipeToKill,
        iconSize,
        fontSize,
        launchApp,
        showQuickSettings
      )
    }
  }
}
