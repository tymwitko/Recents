package com.tymwitko.recents.recentapps

import com.tymwitko.recents.common.dataclasses.App

sealed interface AppListUiState {
  object MissingPermissions : AppListUiState
  object Loading : AppListUiState
  data class EmptyList(val pinnedApps: List<App>) : AppListUiState
  data class Success(
    val list: List<App>,
    val pinnedApps: List<App>,
    val hasPrivileges: Boolean,
    val isSwipeToKill: Boolean
  ) : AppListUiState
}
