package com.tymwitko.recents.viewmodels

import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import com.tymwitko.recents.accessors.AppsAccessor
import com.tymwitko.recents.accessors.IconAccessor
import com.tymwitko.recents.accessors.SystemAppsVisibilityManager
import com.tymwitko.recents.dataclasses.App
import com.tymwitko.recents.whitelist.WhitelistRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WhitelistViewModel(
  private val appsAccessor: AppsAccessor,
  private val systemAppsVisibilityManager: SystemAppsVisibilityManager,
  private val iconAccessor: IconAccessor,
  private val whitelistRepository: WhitelistRepository
) : ViewModel() {

  fun getAllPackages(packageName: String, placeHolderIcon: ImageBitmap?) =
    appsAccessor.getRecentAppsFormatted(
      packageName,
      systemAppsVisibilityManager.shouldShowSystemApps() ?: true
    )
      .filter { !appsAccessor.isLauncher(it) }.toSet().also {
        if (it.isEmpty()) {
          Log.d("TAG", "List empty")
        }
        it.forEachIndexed { ind, t ->
          Log.d("TAG", "App $ind is $t")
        }
      }.map {
        App(
          appsAccessor.getAppName(it).orEmpty(),
          it,
          iconAccessor.getAppIcon(it) ?: placeHolderIcon ?: throw NoSuchElementException()
      )
    }

  fun whitelistAppLaunch(packageName: String, isChecked: Boolean) {
    CoroutineScope(Dispatchers.IO).launch {
      whitelistRepository.setLaunching(packageName, isChecked)
    }
  }

  fun whitelistAppKill(packageName: String, isChecked: Boolean) {
    CoroutineScope(Dispatchers.IO).launch {
      whitelistRepository.setKilling(packageName, isChecked)
    }
  }

  suspend fun toggleSystemApps(areVisible: Boolean, thisPackageName: String) {
    // val systemApps = appsAccessor.getAppsAlphabeticaly(thisPackageName)
    //   .filter { (ApplicationInfo.FLAG_SYSTEM and (it?.flags ?: 0)) != 0 }
    systemAppsVisibilityManager.toggleVisibility(areVisible)
  }
}