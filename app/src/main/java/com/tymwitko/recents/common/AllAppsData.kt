package com.tymwitko.recents.common

import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.settings.whitelist.WhitelistSettingsData

data class AllAppsData(
  val apps: List<App>,
  val filtered: List<App>,
  val pinned: List<App>,
  val settings: MutableMap<String, WhitelistSettingsData>,
  val hasPrivileges: Boolean,
  val isOnlyRunning: Boolean
)
