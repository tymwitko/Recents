package com.tymwitko.recents.recentapps

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
  marginSize: Dp,
  launchApp: (App) -> Unit,
  showQuickSettings: (App, Int, Int) -> Unit,
) {
  val listState = rememberLazyListState()
  LaunchedEffect(appList) {
    listState.animateScrollToItem(0)
  }
  LazyColumn(modifier = modifier, state = listState) {
    items(items = appList, key = { it.getId() }) {
      RecentAppsItem(
        it,
        hasPrivileges,
        isSwipeToKill,
        iconSize,
        fontSize,
        marginSize,
        launchApp,
        showQuickSettings
      )
    }
  }
}
