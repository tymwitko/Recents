package com.tymwitko.recents.settings.whitelist

import com.tymwitko.recents.common.dataclasses.App

sealed interface WhitelistUiState {
  data class Error(val errorMessage: String) : WhitelistUiState
  object Loading : WhitelistUiState
  data class Success(
    val list: List<App>,
    val hasPrivileges: Boolean
  ) : WhitelistUiState
}