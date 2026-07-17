package com.tymwitko.recents.recentapps

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tymwitko.recents.common.FetchAppsUseCase
import com.tymwitko.recents.common.KillAppsUseCase
import com.tymwitko.recents.common.accessors.IntentSender
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.recentapps.quicksettings.WhitelistSettingType
import com.tymwitko.recents.settings.SettingsHolder
import com.tymwitko.recents.settings.whitelist.WhitelistSettingsData
import com.tymwitko.recents.settings.whitelist.db.WhitelistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

class RecentAppsViewModel(
  private val killAppsUseCase: KillAppsUseCase,
  private val intentSender: IntentSender,
  private val whitelistRepository: WhitelistRepository,
  private val fetchAppsUseCase: FetchAppsUseCase,
  private val shizukuManager: ShizukuManager,
  private val settingsHolder: SettingsHolder,
  private val clipboardManager: ClipboardManager
) : ViewModel() {

  private val _uiState = MutableStateFlow<RecentAppsUiState>(RecentAppsUiState.MissingPermissions)
  val uiState: StateFlow<RecentAppsUiState> = _uiState.asStateFlow()

  fun fetchApps(
    thisPackageName: String
  ) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        try {
          if (_uiState.value !is RecentAppsUiState.Success)
            _uiState.emit(RecentAppsUiState.Loading)
          val appData = fetchAppsUseCase(thisPackageName, withFilter = true, withPinned = true)
          _uiState.emit(
            when {
              appData.apps.isEmpty() -> RecentAppsUiState.MissingPermissions
              appData.filtered.isEmpty() -> RecentAppsUiState.EmptyList(appData.pinned)
              else -> RecentAppsUiState.Success(
                appData.filtered,
                appData.pinned,
                appData.settings,
                appData.hasPrivileges,
                isSwipeToKill(),
                appData.isOnlyRunning
              )
            }
          )
        } catch (e: Exception) {
          _uiState.emit(
            RecentAppsUiState.Error(e)
          )
        }
      }
    }
  }

  fun killEmAll(thisPackageName: String, onError: () -> Unit) {
    viewModelScope.launch {
      if (killAppsUseCase.killAll(thisPackageName)) updateAllAfterKill() else onError()
    }
  }

  fun killApp(app: App, onSucc: () -> Unit, onError: () -> Unit) {
    viewModelScope.launch {
      if (killAppsUseCase.killIndividualApp(app)) withContext(Dispatchers.Main) {
        updateAppInState(app, false)
        onSucc()
      }
      else withContext(Dispatchers.Main) { onError() }
    }
  }

  fun launchApp(app: App, startActivity: (Intent, Bundle?) -> Unit) =
    intentSender.launchSelectedApp(app, startActivity)

  fun setupShizuku(thisPackageName: String, onRequest: (Int, Int) -> Unit) {
    shizukuManager.setupPermissionListener(thisPackageName, onRequest)
  }

  fun requestShizuku() {
    try {
      shizukuManager.requestShizukuPermission()
    } catch (_: IllegalStateException) {
      Log.w("TAG", "Shizuku isn't running or is missing entirely")
    }
  }

  fun shutdownShizukuPermissionListener() {
    shizukuManager.shutdownShizukuPermissionListener()
  }

  fun changeWhitelistSetting(app: App, setting: WhitelistSettingType, isChecked: Boolean) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        setWhitelistSetting(app, setting, isChecked)
        updateSingleSetting(
          app.getId(),
          setting,
          isChecked
        )
      }
    }
  }

  fun removeAppFromList(app: App) {
    _uiState.update { old ->
      (old as? RecentAppsUiState.Success)?.list?.minus(app)?.let {
        RecentAppsUiState.Success(
          it,
          old.pinnedApps,
          old.settings,
          old.hasPrivileges,
          old.isSwipeToKill,
          old.isOnlyRunning
        )
      } ?: old
    }
  }

  fun getFontSize() = settingsHolder.getFontSize()

  fun getIconSize(default: Int) = settingsHolder.getIconSize(default)

  fun isOnlyRunning() = settingsHolder.getOnlyRunning()

  fun isSwipeToKill() = isOnlyRunning() && settingsHolder.getSwipeToDelete()

  fun launchAppsInSplitScreen(
    app: App,
    lastApp: App,
    startActivity: (Intent, Bundle?) -> Unit,
    onBothWork: () -> Unit
  ) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        val apps = when {
          !app.isWorkApp -> lastApp to app
          app.isWorkApp && !lastApp.isWorkApp -> app to lastApp
          else -> null
        }
        apps?.let {
          launchApp(apps.first, startActivity)
          delay(500.milliseconds)
          intentSender.goToSplitMode(apps.second, startActivity)
        } ?: run {
          withContext(Dispatchers.Main) {
            onBothWork()
          }
        }
      }
    }
  }

  fun launchFreeForm(
    app: App,
    startActivity: (Intent, Bundle?) -> Unit
  ) {
    intentSender.launchFreeForm(app, startActivity)
  }

  fun updateAllAfterKill() {
    _uiState.update { old ->
      if (isOnlyRunning()) {
        (old as? RecentAppsUiState.Success)?.pinnedApps?.map { App(it, false) }?.let {
          RecentAppsUiState.EmptyList(it)
        } ?: old
      } else (old as? RecentAppsUiState.Success)?.list?.map { App(it, false) }?.let {
        old.copy(
          list = it,
          pinnedApps = old.pinnedApps.map { app -> App(app, false) }
        )
      } ?: old
    }
  }

  fun updateAppInState(app: App, isRunning: Boolean) {
    _uiState.update { old ->
      (old as? RecentAppsUiState.Success)?.let { succ ->
        old.copy(
          list = succ.list.mapNotNull {
            when (it) {
              app if isOnlyRunning() -> null
              app if !isOnlyRunning() -> App(it, isRunning)
              else -> it
            }
          },
          pinnedApps = succ.pinnedApps.map {
            if (it == app) App(it, isRunning) else it
          }
        )
      } ?: old
    }
  }
  
  fun copyToClipboard(content: String) {
    val copy = ClipData.newPlainText("", content)
    clipboardManager.setPrimaryClip(copy)
  }

  fun launchUsageAccessSettings(startActivity: (Intent) -> Unit) {
    intentSender.launchUsageAccessSettings(startActivity)
  }

  private suspend fun setWhitelistSetting(
    app: App,
    setting: WhitelistSettingType,
    toggle: Boolean
  ) {
    withContext(Dispatchers.IO) {
      when (setting) {
        WhitelistSettingType.LAUNCH -> whitelistRepository.setLaunching(app, toggle)
        WhitelistSettingType.KILL -> whitelistRepository.setKilling(app, toggle)
        WhitelistSettingType.SHOW -> whitelistRepository.setShowing(app, toggle)
      }
    }
  }

  private fun updateSingleSetting(
    packageId: String,
    setting: WhitelistSettingType,
    isChecked: Boolean
  ) {
    _uiState.update { old ->
      (old as? RecentAppsUiState.Success)?.let {
        val newSettings = it.settings.toMutableMap().apply {
          put(
            packageId,
            when (setting) {
              WhitelistSettingType.LAUNCH -> get(packageId)?.copy(canLaunch = isChecked)
              WhitelistSettingType.KILL -> get(packageId)?.copy(canKill = isChecked)
              WhitelistSettingType.SHOW -> get(packageId)?.copy(canShow = isChecked)
            } ?: WhitelistSettingsData()
          )
        }
        it.copy(settings = newSettings)
      } ?: old
    }
  }
}
