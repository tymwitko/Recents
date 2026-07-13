package com.tymwitko.recents.settings.pinned

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.tymwitko.recents.common.dataclasses.App
import org.koin.androidx.compose.koinViewModel

@Composable
fun PinnedSettingsList(
  modifier: Modifier = Modifier,
  appList: List<App>,
  fontSize: TextUnit,
  iconSize: Dp,
  pinApp: (App) -> Unit,
  pinnedList: List<App>,
  viewModel: PinnedViewModel = koinViewModel()
) {
  LazyColumn(modifier = modifier) {
    items(items = appList, key = { it.getId() }) {
      PinnedSettingItem(
        app = it,
        iconSize = iconSize,
        fontSize = fontSize,
        isPinned = viewModel.isPinnedByApp(it, pinnedList),
        pinApp = pinApp
      )
    }
  }
}
