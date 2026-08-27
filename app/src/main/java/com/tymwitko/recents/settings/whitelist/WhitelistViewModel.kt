package com.tymwitko.recents.settings.whitelist

import android.content.ClipData
import android.content.ClipboardManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tymwitko.recents.common.FetchAppsUseCase
import com.tymwitko.recents.common.Result
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.settings.SettingsHolder
import com.tymwitko.recents.settings.whitelist.db.WhitelistRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WhitelistViewModel(
  private val whitelistRepository: WhitelistRepository,
  private val settingsHolder: SettingsHolder,
  private val fetchAppsUseCase: FetchAppsUseCase,
  private val clipboardManager: ClipboardManager,
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

  val uiState: StateFlow<WhitelistUiState>
    field = MutableStateFlow<WhitelistUiState>(WhitelistUiState.Loading)

  fun refreshPackages() {
    viewModelScope.launch(dispatcher) {
      if (uiState.value !is WhitelistUiState.Success)
        uiState.emit(WhitelistUiState.Loading)
      uiState.emit(
        when (val appData = fetchAppsUseCase(withFilter = false, withPinned = false)) {
          is Result.Failure -> {
            WhitelistUiState.Error(
              IllegalStateException("List empty!")
            )
          }

          is Result.Success -> {
            WhitelistUiState.Success(
              list = appData.data.apps,
              settings = appData.data.settings,
              hasPrivileges = appData.data.hasPrivileges
            )
          }
        }
      )
    }
  }

  fun whitelistAppLaunch(app: App, isChecked: Boolean) {
    viewModelScope.launch(dispatcher) {
      whitelistRepository.setLaunching(app, isChecked)
      uiState.update { old ->
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

  fun whitelistAppKill(app: App, isChecked: Boolean) {
    viewModelScope.launch(dispatcher) {
      whitelistRepository.setKilling(app, isChecked)
      uiState.update { old ->
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

  fun whitelistAppShow(app: App, isChecked: Boolean) {
    viewModelScope.launch(dispatcher) {
      whitelistRepository.setShowing(app, isChecked)
      uiState.update { old ->
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

  fun getFontSize() = settingsHolder.getFontSize()

  fun getIconSize(default: Int) = settingsHolder.getIconSize(default)

  fun getMarginSize() = settingsHolder.getMarginSize()

  fun copyToClipboard(content: String) {
    val copy = ClipData.newPlainText("", content)
    clipboardManager.setPrimaryClip(copy)
  }
}
