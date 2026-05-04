package com.tymwitko.recents.recentapps

import android.content.Intent
import android.content.pm.PackageInfo
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.scottyab.rootbeer.RootBeer
import com.tymwitko.recents.common.accessors.AppKiller
import com.tymwitko.recents.common.accessors.AppsAccessor
import com.tymwitko.recents.common.accessors.IconAccessor
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
  private val iconAccessor: IconAccessor,
  private val rootBeer: RootBeer,
  private val intentSender: IntentSender,
  private val whitelistRepository: WhitelistRepository,
  private val shizukuManager: ShizukuManager,
  private val uiSettingsHolder: UiSettingsHolder,
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
    var killCount = 0
    appsAccessor.getRecentsAsPackageInfos(thisPackageName)
      .filter { pi ->
        !appKiller.hasAccessibilityService(pi.packageName) &&
          !appKiller.hasSetAlarmPermission(pi.packageName)
      }
      .forEach { pi ->
        killByPackageInfo(
          pi,
          onSucc = { killCount++ },
          onError = {}
        )
      }
    if (killCount == 0) onError()
  }

  fun killByPackageInfo(packageInfo: PackageInfo, onSucc: () -> Unit, onError: () -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
      try {
        appKiller.killByPackageInfo(packageInfo)
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

  private fun getAppIcon(packageName: String) =
    iconAccessor.getAppIcon(packageName)
  
  fun getFontSize() = uiSettingsHolder.getFontSize()

  fun getIconSize(default: Int) = uiSettingsHolder.getIconSize(default)
}
