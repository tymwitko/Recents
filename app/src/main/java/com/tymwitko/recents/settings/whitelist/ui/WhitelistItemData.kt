package com.tymwitko.recents.settings.whitelist.ui

import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.settings.whitelist.WhitelistSettingsData
import kotlinx.coroutines.flow.StateFlow

data class WhitelistItemData(
  val app: App,
  val settings: StateFlow<WhitelistSettingsData?>,
)
