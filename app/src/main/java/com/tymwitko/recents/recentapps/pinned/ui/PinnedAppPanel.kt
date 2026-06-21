package com.tymwitko.recents.recentapps.pinned.ui

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import com.tymwitko.recents.common.dataclasses.App

@Composable
fun PinnedAppPanel(
  apps: List<App>,
  iconSize: Dp,
  launchApp: (App) -> Unit
) {
  LazyRow {
    items(items = apps, key = { it.getId() } ) {
      PinnedAppItem(it, iconSize, launchApp)
    }
  }
}
