package com.tymwitko.recents.settings.pinned

import com.tymwitko.recents.common.dataclasses.App

sealed interface PinnedSettingsUiState {
  object Loading : PinnedSettingsUiState
  data class Error(
    val errorMessage: String
  ) : PinnedSettingsUiState
  data class Success(
    val list: List<App>,
    val pinned: List<App>
  ) : PinnedSettingsUiState
}