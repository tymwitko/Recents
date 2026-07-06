package com.tymwitko.recents.recentapps

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scottyab.rootbeer.RootBeer
import com.tymwitko.recents.common.accessors.AppKiller
import com.tymwitko.recents.common.accessors.AppsAccessor
import com.tymwitko.recents.common.accessors.IntentSender
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.common.distinctByNamePickApp
import com.tymwitko.recents.recentapps.pinned.db.PinnedAppDetails
import com.tymwitko.recents.recentapps.pinned.db.PinnedRepository
import com.tymwitko.recents.settings.SettingsHolder
import com.tymwitko.recents.settings.whitelist.WhitelistSettingsData
import com.tymwitko.recents.settings.whitelist.db.WhitelistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecentAppsViewModel(
  private val appsAccessor: AppsAccessor,
  private val appKiller: AppKiller,
  private val rootBeer: RootBeer,
  private val intentSender: IntentSender,
  private val whitelistRepository: WhitelistRepository,
  private val shizukuManager: ShizukuManager,
  private val settingsHolder: SettingsHolder,
  private val pinnedRepository: PinnedRepository
) : ViewModel() {

  private val _uiState = MutableStateFlow<RecentAppsUiState>(RecentAppsUiState.MissingPermissions)
  val uiState: StateFlow<RecentAppsUiState> = _uiState.asStateFlow()

  private val settings = HashMap<String, MutableStateFlow<WhitelistSettingsData>>()

  private val _hasPrivileges = MutableStateFlow(false)
  val hasPrivileges: StateFlow<Boolean>
    get() = _hasPrivileges

  suspend fun getApps(
    thisPackageName: String,
    hasPrivileges: Boolean
  ): MutableList<App> {
    return appsAccessor.getRecentApps(hasPrivileges).toList()
      .filter {
        it.packageName != thisPackageName &&
            !appsAccessor.isLauncher(it.packageName)
      }
      .onEach {
        viewModelScope.launch {
          withContext(Dispatchers.IO) {
            settings[it.getId()] = MutableStateFlow(
              WhitelistSettingsData(canLaunch = true, canKill = true, canShow = true)
            )
            whitelistRepository.getEntry(it.getId())?.let { packageSettings ->
              settings[it.getId()]?.update {
                WhitelistSettingsData(
                  packageSettings.canLaunch,
                  packageSettings.canKill,
                  packageSettings.canShow
                )
              }
            }
          }
        }
      }
      .distinctByNamePickApp()
      .sortedByDescending { it.lastTimeUsed }
      .toMutableList()
  }

  fun fetchApps(
    thisPackageName: String
  ) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        if (_uiState.value !is RecentAppsUiState.Success) _uiState.emit(RecentAppsUiState.Loading)
        val hasPrivileges = hasPrivileges()
        val fullList = getApps(thisPackageName, hasPrivileges)
        val filtered = fullList.filter {
          whitelistRepository.canShow(it.getId()) && (!isOnlyRunning() || it.isRunning)
        }.toMutableList()
        val pinned = pinnedRepository.getAllPinned()
        val pinnedApps =
          fullList.filter {
            PinnedAppDetails(it) in pinned
          }.toMutableList()
        _uiState.emit(
          when {
            fullList.isEmpty() -> RecentAppsUiState.MissingPermissions
            filtered.isEmpty() -> RecentAppsUiState.EmptyList(pinnedApps)
            else -> RecentAppsUiState.Success(filtered, pinnedApps, hasPrivileges, isSwipeToKill())
          }
        )
      }
    }
  }

  fun killEmAll(thisPackageName: String, onError: () -> Unit) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        var killCount = 0
        appsAccessor.getRecentApps(hasPrivileges())
          .filter { it.packageName != thisPackageName }
          .let {
            it.collect { app ->
              if (killIndividualApp(app)) killCount++
            }
            if (killCount == 0) withContext(Dispatchers.Main) {
              onError()
            } else {
              updateAllAfterKill()
            }
          }
      }
    }
  }

  fun killApp(app: App, onSucc: () -> Unit, onError: () -> Unit) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        if (killIndividualApp(app)) withContext(Dispatchers.Main) {
          updateAppInState(app, false)
          onSucc()
        }
        else withContext(Dispatchers.Main) { onError() }
      }
    }
  }

  suspend fun killIndividualApp(app: App) =
    runCatching {
      withContext(Dispatchers.IO) {
        appKiller.killApp(app)
        true
      }
    }.getOrDefault(false)

  private fun hasPrivileges() = shizukuManager.isShizukuAllowed() || rootBeer.isRooted

  fun checkPrivileges() {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        _hasPrivileges.update { hasPrivileges() }
      }
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

  fun whitelistAppLaunch(app: App, isChecked: Boolean) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        whitelistRepository.setLaunching(app, isChecked)
        Log.d("TAG", "launching set to $isChecked for ${app.packageName}")
        updateSingleSetting(app.getId(), canLaunch = isChecked)
      }
    }
  }

  fun whitelistAppKill(app: App, isChecked: Boolean) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        whitelistRepository.setKilling(app, isChecked)
        Log.d("TAG", "killing set to $isChecked for ${app.packageName}")
        updateSingleSetting(app.getId(), canKill = isChecked)
      }
    }
  }

  fun whitelistAppShow(app: App, isChecked: Boolean) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        whitelistRepository.setShowing(app, isChecked)
        Log.d("TAG", "showing set to $isChecked for ${app.packageName}")
        updateSingleSetting(app.getId(), canShow = isChecked)
      }
    }
  }

  fun removeAppFromList(app: App) {
    _uiState.update { old ->
      (old as? RecentAppsUiState.Success)?.list?.minus(app)?.let {
        RecentAppsUiState.Success(it, old.pinnedApps, old.hasPrivileges, old.isSwipeToKill)
      } ?: old
    }
  }

  fun getSettingsForApp(packageId: String): StateFlow<WhitelistSettingsData>? =
    settings[packageId]

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
          Thread.sleep(500)
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

  private fun updateSingleSetting(
    packageId: String,
    canLaunch: Boolean? = null,
    canKill: Boolean? = null,
    canShow: Boolean? = null
  ) {
    settings[packageId]?.let {
      it.value = when {
        canLaunch != null -> it.value.copy(
          canLaunch = canLaunch
        )

        canKill != null -> it.value.copy(
          canKill = canKill
        )

        canShow != null -> it.value.copy(
          canShow = canShow
        )

        else -> it.value
      }
    } ?: run {
      settings[packageId] = MutableStateFlow(
        WhitelistSettingsData(
          canLaunch = canLaunch ?: true,
          canKill = canKill ?: true,
          canShow = canShow ?: true
        )
      )
    }
  }

  fun updateAllAfterKill() {
    _uiState.update { old ->
      if (isOnlyRunning()) {
        (old as? RecentAppsUiState.EmptyList)?.pinnedApps?.map { App(it, false) }?.let {
          RecentAppsUiState.EmptyList(it)
        } ?: old
      }
      else (old as? RecentAppsUiState.Success)?.list?.map { App(it, false) }?.let {
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
}
