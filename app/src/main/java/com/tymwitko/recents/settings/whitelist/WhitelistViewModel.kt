package com.tymwitko.recents.settings.whitelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tymwitko.recents.common.FetchAppsUseCase
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.settings.SettingsHolder
import com.tymwitko.recents.settings.whitelist.db.WhitelistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WhitelistViewModel(
  private val whitelistRepository: WhitelistRepository,
  private val settingsHolder: SettingsHolder,
  private val fetchAppsUseCase: FetchAppsUseCase
) : ViewModel() {

  private val _uiState = MutableStateFlow<WhitelistUiState>(WhitelistUiState.Loading)
  val uiState: StateFlow<WhitelistUiState> = _uiState.asStateFlow()

  fun refreshPackages(thisPackageName: String) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        try {
          if (_uiState.value !is WhitelistUiState.Success)
            _uiState.emit(WhitelistUiState.Loading)
          fetchAppsUseCase(thisPackageName, false)
            .let { appData ->
              _uiState.emit(
                if (appData.apps.isNotEmpty()) WhitelistUiState.Success(
                  list = appData.apps,
                  settings = appData.settings,
                  hasPrivileges = appData.hasPrivileges
                ) else WhitelistUiState.Error("List empty, but no error was thrown!")
              )
            }
        } catch (e: Exception) {
          _uiState.emit(
            WhitelistUiState.Error(e.stackTraceToString())
          )
        }
      }
    }
  }

  fun whitelistAppLaunch(app: App, isChecked: Boolean) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        whitelistRepository.setLaunching(app, isChecked)
        _uiState.update { old ->
          (old as? WhitelistUiState.Success)?.let {
            val newSettings = it.settings.toMutableMap().apply {
              put(
                app.getId(),
                (get(app.getId())?.copy(canLaunch = isChecked)
                  ?: WhitelistSettingsData(canLaunch = isChecked))
              )
            }
            it.copy(settings = newSettings)
          } ?: old
        }
      }
    }
  }

  fun whitelistAppKill(app: App, isChecked: Boolean) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        whitelistRepository.setKilling(app, isChecked)
        _uiState.update { old ->
          (old as? WhitelistUiState.Success)?.let {
            val newSettings = it.settings.toMutableMap().apply {
              put(
                app.getId(),
                (get(app.getId())?.copy(canKill = isChecked)
                  ?: WhitelistSettingsData(canKill = isChecked))
              )
            }
            it.copy(settings = newSettings)
          } ?: old
        }
      }
    }
  }

  fun whitelistAppShow(app: App, isChecked: Boolean) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        whitelistRepository.setShowing(app, isChecked)
        _uiState.update { old ->
          (old as? WhitelistUiState.Success)?.let {
            val newSettings = it.settings.toMutableMap().apply {
              put(
                app.getId(),
                (get(app.getId())?.copy(canShow = isChecked)
                  ?: WhitelistSettingsData(canShow = isChecked))
              )
            }
            it.copy(settings = newSettings)
          } ?: old
        }
      }
    }
  }

  fun getFontSize() = settingsHolder.getFontSize()

  fun getIconSize(default: Int) = settingsHolder.getIconSize(default)
}
