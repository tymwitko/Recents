package com.tymwitko.recents.settings.pinned

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tymwitko.recents.common.dataclasses.App
import org.koin.androidx.compose.koinViewModel

@Composable
fun PinnedSettingsList(
  modifier: Modifier = Modifier,
  appList: List<App>,
  fontSize: TextUnit,
  iconSize: Dp,
  pinApp: (App) -> Unit,
  viewModel: PinnedViewModel = koinViewModel()
) {
  val arePinned by viewModel.isPinned.collectAsStateWithLifecycle()
  LaunchedEffect(appList) {
    viewModel.fetchSettings()
  }
  LazyColumn(modifier = modifier) {
    items(items = appList, key = { it.packageName + it.isWorkApp.toString() }) {
      PinnedSettingItem(
        app = it,
        iconSize = iconSize,
        fontSize = fontSize,
        isPinned = viewModel.isPinnedByApp(it, arePinned),
        pinApp = pinApp
      )
    }
  }
}
