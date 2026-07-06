package com.tymwitko.recents.recentapps

import com.tymwitko.recents.common.dataclasses.App

sealed interface RecentAppsUiState {
  object MissingPermissions : RecentAppsUiState
  object Loading : RecentAppsUiState
  data class EmptyList(val pinnedApps: List<App>) : RecentAppsUiState
  data class Success(
    val list: List<App>,
    val pinnedApps: List<App>,
    val hasPrivileges: Boolean,
    val isSwipeToKill: Boolean
  ) : RecentAppsUiState
}
