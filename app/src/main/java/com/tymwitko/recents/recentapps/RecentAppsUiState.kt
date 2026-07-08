package com.tymwitko.recents.recentapps

import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.settings.whitelist.WhitelistSettingsData

sealed interface RecentAppsUiState {
  object MissingPermissions : RecentAppsUiState
  object Loading : RecentAppsUiState
  data class EmptyList(val pinnedApps: List<App>) : RecentAppsUiState
  data class Success(
    val list: List<App>,
    val pinnedApps: List<App>,
    val settings: MutableMap<String, WhitelistSettingsData>,
    val hasPrivileges: Boolean,
    val isSwipeToKill: Boolean
  ) : RecentAppsUiState
  data class Error(val errorMessage: String) : RecentAppsUiState
}
