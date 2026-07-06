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

  private val settings = HashMap<String, MutableStateFlow<WhitelistSettingsData?>>()

  private val _uiState = MutableStateFlow<WhitelistUiState>(WhitelistUiState.MissingPermissions)
  val uiState: StateFlow<WhitelistUiState> = _uiState.asStateFlow()

  fun refreshPackages(thisPackageName: String) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        if (_uiState.value !is WhitelistUiState.Success)
          _uiState.emit(WhitelistUiState.Loading)
        val privileges = hasPrivileges()
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
                settings[app.getId()] = MutableStateFlow(null)
                whitelistRepository.getEntry(app.getId())?.let { packageSettings ->
                  settings[app.getId()]?.update {
                    WhitelistSettingsData(
                      packageSettings.canLaunch,
                      packageSettings.canKill,
                      packageSettings.canShow
                    )
                  }
                }
              }
              .let { newList ->
                _uiState.emit(
                  if (newList.isNotEmpty()) WhitelistUiState.Success(
                    list = newList,
                    hasPrivileges = privileges
                  ) else WhitelistUiState.MissingPermissions
                )
              }
          }
      }
    }
  }

  fun whitelistAppLaunch(app: App, isChecked: Boolean) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        whitelistRepository.setLaunching(app, isChecked)
      }
    }
  }

  fun whitelistAppKill(app: App, isChecked: Boolean) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        whitelistRepository.setKilling(app, isChecked)
      }
    }
  }

  fun whitelistAppShow(app: App, isChecked: Boolean) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        whitelistRepository.setShowing(app, isChecked)
      }
    }
  }

  fun getSettingsForApp(packageId: String): StateFlow<WhitelistSettingsData?> =
    settings[packageId] ?: MutableStateFlow(
      WhitelistSettingsData(
        canLaunch = true,
        canKill = true,
        canShow = true
      )
    )

  fun getFontSize() = settingsHolder.getFontSize()

  fun getIconSize(default: Int) = settingsHolder.getIconSize(default)

  private fun hasPrivileges() = shizukuManager.isShizukuAllowed() || rootBeer.isRooted
}
