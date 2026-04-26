package com.tymwitko.recents.recentapps

import android.content.Intent
import android.content.pm.PackageInfo
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
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
  private val uiSettingsHolder: UiSettingsHolder
) : ViewModel() {

  val appList: MutableLiveData<List<App>> = MutableLiveData()
  private val settings = HashMap<String, MutableLiveData<WhitelistSettingsData>>()

  private fun getActivePackages(
    thisPackageName: String,
  ): Set<String> {
    return appsAccessor.getRecentAppsFormatted(thisPackageName)
      .filter { !appsAccessor.isLauncher(it) }
      .onEach {
        CoroutineScope(Dispatchers.IO).launch {
          settings[it] = MutableLiveData()
          whitelistRepository.getEntry(it)?.let { packageSettings ->
            settings[it]?.postValue(
              WhitelistSettingsData(
                packageSettings.canLaunch,
                packageSettings.canKill,
                packageSettings.canShow
              )
            )
          }
        }
      }
      .toSet()
  }

  fun getActiveApps(
    thisPackageName: String,
    placeHolderIcon: ImageBitmap?
  ) = getActivePackages(thisPackageName).map {
    App(
      appsAccessor.getAppName(it).orEmpty(),
      it,
      getAppIcon(it) ?: placeHolderIcon ?: throw NoSuchElementException()
    )
  }

  fun getActiveAppsFiltered(
    thisPackageName: String,
    placeHolderIcon: ImageBitmap?
  ) {
    CoroutineScope(Dispatchers.IO).launch {
      appList.postValue(
        getActivePackages(thisPackageName)
          .filter { whitelistRepository.canShow(it) }
          .map {
          App(
            appsAccessor.getAppName(it).orEmpty(),
            it,
            getAppIcon(it) ?: placeHolderIcon ?: throw NoSuchElementException()
          )
        }
      )
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

  fun launchApp(packageName: String, startActivity: (Intent) -> Unit) =
    intentSender.launchSelectedApp(packageName, startActivity)

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
