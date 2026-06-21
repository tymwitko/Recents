package com.tymwitko.recents.lastapp

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModel
import com.scottyab.rootbeer.RootBeer
import com.tymwitko.recents.common.accessors.AppsAccessor
import com.tymwitko.recents.common.accessors.IntentSender
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.settings.SettingsHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext

class LastAppViewModel(
  private val intentSender: IntentSender,
  private val appsAccessor: AppsAccessor,
  private val rootBeer: RootBeer,
  private val shizukuManager: ShizukuManager,
  private val settingsHolder: SettingsHolder
) : ViewModel() {

  suspend fun launchLastApp(startActivity: (Intent, Bundle?) -> Unit, thisPackageName: String) {
    withContext(Dispatchers.Default) {
      appsAccessor.getRecentApps(hasPrivileges(), isOnlyRunning())
        .filter { it.packageName != thisPackageName }
        .toList()
        .sortedByDescending { it.lastTimeUsed }
        .drop(1)
        .filter { appsAccessor.shouldLaunch(it) }
        .let { intentSender.launchLastApp(it.toList(), startActivity) }
    }
  }

  private fun hasPrivileges() = shizukuManager.isShizukuAllowed() || rootBeer.isRooted

  private fun isOnlyRunning() = settingsHolder.getOnlyRunning()
}
