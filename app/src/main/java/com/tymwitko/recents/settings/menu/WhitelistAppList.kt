package com.tymwitko.recents.settings.menu

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.LifecycleOwner
import com.tymwitko.recents.settings.whitelist.ui.SettingItem
import com.tymwitko.recents.settings.whitelist.ui.WhitelistItem

@Composable
fun WhitelistAppList(
  modifier: Modifier = Modifier,
  appList: List<SettingItem>,
  whitelistLaunch: (String, Boolean) -> Unit,
  whitelistKill: (String, Boolean) -> Unit,
  whitelistShow: (String, Boolean) -> Unit,
  lifecycleOwner: LifecycleOwner,
  showKillCheck: Boolean
) {
  LazyColumn(modifier = modifier) {
    items(items = appList) {
      WhitelistItem(
        it.app.name,
        it.app.packageName,
        it.app.icon,
        showKillCheck,
        whitelistLaunch,
        whitelistKill,
        whitelistShow,
        it.settings,
        lifecycleOwner
      )
    }
  }
}
