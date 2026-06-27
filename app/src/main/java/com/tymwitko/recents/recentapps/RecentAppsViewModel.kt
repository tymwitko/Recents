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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
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

  private val settings = HashMap<String, MutableStateFlow<WhitelistSettingsData>>()

  private val _appList = MutableStateFlow<List<App>?>(null)

  val appList: StateFlow<List<App>?>
    get() = _appList

  private val _pinnedApps = MutableStateFlow<List<App>?>(null)

  val pinnedApps: StateFlow<List<App>?>
    get() = _pinnedApps

  private val _hasPrivileges = MutableStateFlow(false)
  val hasPrivileges: StateFlow<Boolean>
    get() = _hasPrivileges

  suspend fun getApps(
    thisPackageName: String,
    onlyRunning: Boolean
  ): MutableList<App> {
    return appsAccessor.getRecentApps(hasPrivileges(), onlyRunning).toList()
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
        val fullList = getApps(thisPackageName, isOnlyRunning())
        _appList.update {
          fullList
            .filter {
              whitelistRepository.canShow(it.getId()) &&
                (!isOnlyRunning() || it.isRunning)
            }.toMutableList()
        }
        if (!isOnlyRunning()) {
          val pinned = pinnedRepository.getAllPinned()
          _pinnedApps.update {
            fullList.filter { app ->
              checkIfPinned(pinned, app)
            }.toMutableList()
          }
        }
      }
    }
    if (isOnlyRunning()) {
      viewModelScope.launch {
        withContext(Dispatchers.IO) {
          val pinned = flowOf(pinnedRepository.getAllPinned())
          val fullList = flowOf(getApps(thisPackageName, false))

          val combo = pinned.combine(fullList) { pin, full ->
            (full to pin)
          }
          combo.collect { results ->
            _pinnedApps.update {
              results.first.filter { app ->
                checkIfPinned(results.second, app)
              }.toMutableList()
            }
          }
        }
      }
    }
  }

  fun killEmAll(thisPackageName: String, onError: () -> Unit) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        var killCount = 0
        appsAccessor.getRecentApps(hasPrivileges(), isOnlyRunning())
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
          updateAppInList(app, false)
          updateAppInPinned(app, false)
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
    _appList.update { _appList.value?.minus(app) }
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

  private fun checkIfPinned(pinnedApps: List<PinnedAppDetails>, app: App): Boolean {
    val pinnedFromApp = PinnedAppDetails(app)
    return pinnedApps.any { it == pinnedFromApp }
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

  fun updateAppInPinned(app: App, isRunning: Boolean) {
    _pinnedApps.update { oldList ->
      oldList?.map {
        if (it == app) App(it, isRunning) else it
      }
    }
  }
  
  fun updateAllAfterKill() {
    _pinnedApps.update { oldList ->
      oldList?.map { App(it, false) }
    }
    _appList.update { oldList ->
      if (isOnlyRunning()) listOf() else oldList?.map { App(it, false) }
    }
  }
  
  fun updateAppInList(app: App, isRunning: Boolean) {
    _appList.update { oldList ->
      oldList?.map {
        if (it == app) App(it, isRunning) else it
      }
    }
  }
}
