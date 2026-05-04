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
import com.tymwitko.recents.common.exceptions.AppNotKilledException
import com.tymwitko.recents.settings.ui.UiSettingsHolder
import com.tymwitko.recents.settings.whitelist.WhitelistSettingsData
import com.tymwitko.recents.settings.whitelist.db.WhitelistRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecentAppsViewModel(
  private val appsAccessor: AppsAccessor,
  private val appKiller: AppKiller,
  private val rootBeer: RootBeer,
  private val intentSender: IntentSender,
  private val whitelistRepository: WhitelistRepository,
  private val shizukuManager: ShizukuManager,
  private val uiSettingsHolder: UiSettingsHolder
) : ViewModel() {

  private val settings = HashMap<String, MutableLiveData<WhitelistSettingsData>>()

  private val _appList = MutableStateFlow<List<App>?>(null)

  val appList: StateFlow<List<App>?>
    get() = _appList

  suspend fun getActiveApps(
    thisPackageName: String
  ): List<App> {
    return appsAccessor.getRecentApps(thisPackageName, hasPrivileges())
      .filter { !appsAccessor.isLauncher(it.name) }
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
      .distinctBy { it.packageName }
  }

  fun getActiveAppsFiltered(
    thisPackageName: String
  ) {
    CoroutineScope(Dispatchers.IO).launch {
      _appList.value = getActiveApps(thisPackageName)
        .filter { whitelistRepository.canShow(it.packageName) }
    }
  }

  fun killEmAll(thisPackageName: String, onError: () -> Unit) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        var killCount = 0
        appsAccessor.getRecentApps(thisPackageName, hasPrivileges())
          .filter { app ->
            !appKiller.hasAccessibilityService(app.packageName) &&
              !appKiller.hasSetAlarmPermission(app.packageName) &&
              app.packageName != thisPackageName
          }
          .forEach { app ->
            if (killIndividualPackage(app.packageName)) killCount++
          }
        if (killCount == 0) withContext(Dispatchers.Main) {
          onError()
        }
      }
    }
  }

  fun killByPackageName(packageName: String, onSucc: () -> Unit, onError: () -> Unit) {
    viewModelScope.launch { 
      withContext(Dispatchers.IO) {
        try {
          killIndividualPackage(packageName)
          withContext(Dispatchers.Main) {
            onSucc()
          }
        } catch (_: AppNotKilledException) {
          withContext(Dispatchers.Main) {
            onError()
          }
        }
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

  fun hasPrivileges() = shizukuManager.isShizukuAllowed() || rootBeer.isRooted

  fun launchApp(app: App, startActivity: (Intent) -> Unit) =
    intentSender.launchSelectedApp(app, startActivity)

  fun hideSystemApps(apps: List<App>) {
    CoroutineScope(Dispatchers.IO).launch {
      apps.filter {
        appsAccessor.isSystemApp(it.packageName)
      }.forEach {
        whitelistRepository.setDefaultShowing(it.packageName, false)
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

  fun shutdownShizuku() {
    shizukuManager.shutdownShizuku()
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

  fun getSettingsForApp(packageName: String) = settings[packageName]

  fun getFontSize() = uiSettingsHolder.getFontSize()

  fun getIconSize(default: Int) = uiSettingsHolder.getIconSize(default)
}
