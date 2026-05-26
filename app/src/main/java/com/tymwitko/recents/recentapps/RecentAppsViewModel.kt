package com.tymwitko.recents.recentapps

import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scottyab.rootbeer.RootBeer
import com.tymwitko.recents.common.accessors.AppKiller
import com.tymwitko.recents.common.accessors.AppsAccessor
import com.tymwitko.recents.common.accessors.IntentSender
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.common.dataclasses.DumpApp
import com.tymwitko.recents.settings.SettingsHolder
import com.tymwitko.recents.settings.whitelist.WhitelistSettingsData
import com.tymwitko.recents.settings.whitelist.db.WhitelistRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
  private val settingsHolder: SettingsHolder
) : ViewModel() {

  private val settings = HashMap<String, MutableLiveData<WhitelistSettingsData>>()

  private val _appList = MutableStateFlow<MutableList<App>?>(null)

  val appList: StateFlow<List<App>?>
    get() = _appList

  private val _hasPrivileges = MutableStateFlow(false)
  val hasPrivileges: StateFlow<Boolean>
    get() = _hasPrivileges

  suspend fun getApps(
    thisPackageName: String,
    onlyRunning: Boolean
  ): MutableList<App> {
    return appsAccessor.getRecentApps(hasPrivileges(), isOnlyRunning()).toList()
      .filter {
        it.packageName != thisPackageName &&
          !appsAccessor.isLauncher(it.packageName) &&
          whitelistRepository.canShow(it.packageName) &&
          (!onlyRunning || it.isRunning)
      }
      .onEach {
        CoroutineScope(Dispatchers.IO).launch {
          settings[it.packageName] = MutableLiveData()
          whitelistRepository.getEntry(it.packageName)?.let { packageSettings ->
            settings[it.packageName]?.postValue(
              WhitelistSettingsData(
                packageSettings.canLaunch,
                packageSettings.canKill,
                packageSettings.canShow
              )
            )
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
    CoroutineScope(Dispatchers.IO).launch {
      _appList.value = getApps(thisPackageName, isOnlyRunning())
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
              if (killIndividualPackage(app.packageName)) killCount++
            }
            if (killCount == 0) withContext(Dispatchers.Main) {
              onError()
            }
          }
      }
    }
  }

  fun killByPackageName(packageName: String, onSucc: () -> Unit, onError: () -> Unit) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        if (killIndividualPackage(packageName)) withContext(Dispatchers.Main) { onSucc() }
        else withContext(Dispatchers.Main) { onError() }
      }
    }
  }

  suspend fun killIndividualPackage(packageName: String) =
    runCatching {
      withContext(Dispatchers.IO) {
        appKiller.killByPackageName(packageName)
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

  fun launchApp(app: App, startActivity: (Intent) -> Unit) =
    intentSender.launchSelectedApp(app, startActivity)

  fun hideSystemApps(apps: List<App>) {
    CoroutineScope(Dispatchers.IO).launch {
      apps.filter {
        appsAccessor.isSystemApp(it.packageName)
      }.forEach {
        whitelistRepository.setDefaultWhitelistSettings(
          it.packageName,
          canShow = false,
          canKill = false
        )
      }
    }
  }

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

  fun whitelistAppLaunch(packageName: String, isChecked: Boolean) {
    CoroutineScope(Dispatchers.IO).launch {
      whitelistRepository.setLaunching(packageName, isChecked)
      Log.d("TAG", "launching set to $isChecked for $packageName")
    }
  }

  fun whitelistAppKill(packageName: String, isChecked: Boolean) {
    CoroutineScope(Dispatchers.IO).launch {
      whitelistRepository.setKilling(packageName, isChecked)
      Log.d("TAG", "killing set to $isChecked for $packageName")
    }
  }

  fun whitelistAppShow(packageName: String, isChecked: Boolean) {
    CoroutineScope(Dispatchers.IO).launch {
      whitelistRepository.setShowing(packageName, isChecked)
      Log.d("TAG", "showing set to $isChecked for $packageName")
    }
  }
  
  fun removeAppFromList(app: App) {
    _appList.value = _appList.value?.toMutableList()?.apply { remove(app) }
  }

  fun getSettingsForApp(packageName: String) = settings[packageName]

  fun getFontSize() = settingsHolder.getFontSize()

  fun getIconSize(default: Int) = settingsHolder.getIconSize(default)

  fun isOnlyRunning() = settingsHolder.getOnlyRunning()
  
  fun isSwipeToKill() = isOnlyRunning() && settingsHolder.getSwipeToDelete()
  
  fun List<App>.distinctByNamePickApp(): List<App> =
    groupBy { it.packageName }
      .map {
        it.value.filter { app -> app as? DumpApp == null }
          .takeIf { it.isNotEmpty() }
          ?.maxByOrNull { it.lastTimeUsed ?: 0L } ?: it.value.first()
      }
}
