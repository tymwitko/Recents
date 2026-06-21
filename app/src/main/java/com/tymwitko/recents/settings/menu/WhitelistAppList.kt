package com.tymwitko.recents.settings.menu

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.settings.whitelist.ui.WhitelistItem
import com.tymwitko.recents.settings.whitelist.ui.WhitelistItemData

@Composable
fun WhitelistAppList(
  modifier: Modifier = Modifier,
  appList: List<WhitelistItemData>,
  fontSize: TextUnit,
  iconSize: Dp,
  whitelistLaunch: (App, Boolean) -> Unit,
  whitelistKill: (App, Boolean) -> Unit,
  whitelistShow: (App, Boolean) -> Unit,
  showKillCheck: Boolean
) {
  LazyColumn(modifier = modifier) {
    items(items = appList, key = { it.app.getId() }) {
      WhitelistItem(
        it.app,
        showKillCheck,
        fontSize,
        iconSize,
        whitelistLaunch,
        whitelistKill,
        whitelistShow,
        it.settings
      )
    }
  }
}
