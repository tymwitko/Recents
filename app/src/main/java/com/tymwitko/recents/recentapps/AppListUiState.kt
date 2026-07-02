package com.tymwitko.recents.recentapps

import com.tymwitko.recents.common.dataclasses.App

sealed interface AppListUiState {
    object MissingPermissions : AppListUiState
    object Loading : AppListUiState
    object EmptyList : AppListUiState
    data class Success(val list: List<App>) : AppListUiState
}
