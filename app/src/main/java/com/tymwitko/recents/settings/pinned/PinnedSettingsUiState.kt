package com.tymwitko.recents.settings.pinned

import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.recentapps.pinned.db.PinnedAppDetails

sealed interface PinnedSettingsUiState {
  object Loading : PinnedSettingsUiState
  object MissingPermissions : PinnedSettingsUiState
  data class Success(
    val list: List<App>,
    val pinned: List<PinnedAppDetails>
  ) : PinnedSettingsUiState
}