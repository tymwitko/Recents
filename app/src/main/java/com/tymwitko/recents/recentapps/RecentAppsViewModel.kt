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
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.common.exceptions.AppNotKilledException
import com.tymwitko.recents.whitelist.db.WhitelistRepository
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
  private val whitelistRepository: WhitelistRepository
) : ViewModel() {

  val appList: MutableLiveData<List<App>> = MutableLiveData()

  private fun getActivePackages(
    thisPackageName: String,
  ): Set<String> {
    return appsAccessor.getRecentAppsFormatted(thisPackageName)
      .filter { !appsAccessor.isLauncher(it) }
      .toSet()
      .also {
        if (it.isEmpty()) {
          Log.d("TAG", "List empty")
        }
        it.forEachIndexed { ind, t ->
          Log.d("TAG", "App $ind is $t")
        }
      }
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
      } catch (e: AppNotKilledException) {
        withContext(Dispatchers.Main) {
          onError()
        }
      }
    }
  }

  fun hasRoot() = rootBeer.isRooted

  fun launchApp(packageName: String, startActivity: (Intent) -> Unit) =
    intentSender.launchSelectedApp(packageName, startActivity)

  private fun getAppIcon(packageName: String) =
    iconAccessor.getAppIcon(packageName)
}
