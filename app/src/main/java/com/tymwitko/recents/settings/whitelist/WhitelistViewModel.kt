package com.tymwitko.recents.settings.whitelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scottyab.rootbeer.RootBeer
import com.tymwitko.recents.common.accessors.AppsAccessor
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.settings.SettingsHolder
import com.tymwitko.recents.settings.whitelist.db.WhitelistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WhitelistViewModel(
  private val appsAccessor: AppsAccessor,
  private val whitelistRepository: WhitelistRepository,
  private val rootBeer: RootBeer,
  private val shizukuManager: ShizukuManager,
  private val settingsHolder: SettingsHolder
) : ViewModel() {

  private val _uiState = MutableStateFlow<WhitelistUiState>(WhitelistUiState.Loading)
  val uiState: StateFlow<WhitelistUiState> = _uiState.asStateFlow()

  fun refreshPackages(thisPackageName: String) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        try {
          if (_uiState.value !is WhitelistUiState.Success)
            _uiState.emit(WhitelistUiState.Loading)
          val privileges = hasPrivileges()
          val settings = mutableMapOf<String, WhitelistSettingsData>()
          appsAccessor.getRecentApps(
            privileges,
            false
          )
            .let { apps ->
              apps.toList()
                .distinctBy { it.getId() }
                .filter {
                  it.packageName != thisPackageName && !appsAccessor.isLauncher(it.packageName)
                }
                .onEach { app ->
                  whitelistRepository.getEntry(app.getId())?.let { packageSettings ->
                    settings[app.getId()] = WhitelistSettingsData(
                      packageSettings.canLaunch,
                      packageSettings.canKill,
                      packageSettings.canShow
                    )
                  }
                }
                .let { newList ->
                  _uiState.emit(
                    if (newList.isNotEmpty()) WhitelistUiState.Success(
                      list = newList,
                      settings = settings,
                      hasPrivileges = privileges
                    ) else WhitelistUiState.Error("List empty, but no error was thrown!")
                  )
                }
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
                  ?: WhitelistSettingsData(canLaunch = isChecked, canKill = true, canShow = true))
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
                  ?: WhitelistSettingsData(canLaunch = true, canKill = isChecked, canShow = true))
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
                  ?: WhitelistSettingsData(canLaunch = true, canKill = true, canShow = isChecked))
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

  private fun hasPrivileges() = shizukuManager.isShizukuAllowed() || rootBeer.isRooted
}
