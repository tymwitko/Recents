package com.tymwitko.recents.settings.whitelist.ui

import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.settings.whitelist.WhitelistSettingsData

data class WhitelistItemData(
  val app: App,
  val settings: WhitelistSettingsData?,
)
