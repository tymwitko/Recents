package com.tymwitko.recents.lastapp

import android.content.Intent
import android.os.Bundle
import com.scottyab.rootbeer.RootBeer
import com.tymwitko.recents.common.accessors.AppsAccessor
import com.tymwitko.recents.common.accessors.IntentSender
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.common.distinctByNamePickApp
import com.tymwitko.recents.settings.SettingsHolder
import com.tymwitko.recents.settings.whitelist.WhitelistSettingsData
import com.tymwitko.recents.settings.whitelist.db.WhitelistRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList

class LaunchLastAppUseCase(
  private val appsAccessor: AppsAccessor,
  private val whitelistRepository: WhitelistRepository,
  private val settingsHolder: SettingsHolder,
  private val shizukuManager: ShizukuManager,
  private val rootBeer: RootBeer,
  private val intentSender: IntentSender
) {
  suspend operator fun invoke(
    thisPackageName: String,
    startActivity: (Intent, Bundle?) -> Unit
  ): Boolean = coroutineScope {
    val privileges = hasPrivileges()
    val onlyRunning = isOnlyRunning()

    val fullDeferred = async {
      appsAccessor.getRecentApps(privileges).toList()
        .filter {
          it.packageName != thisPackageName && !appsAccessor.isLauncher(it.packageName)
        }
        .distinctByNamePickApp()
        .sortedByDescending { it.lastTimeUsed }
        .toMutableList()
    }

    val whiteListDeferred = async {
      whitelistRepository.getAllEntries()
    }

    val fullList = fullDeferred.await()
    val fullWhitelist = whiteListDeferred.await()

    val settings = mutableMapOf<String, WhitelistSettingsData>()
    fullList.forEach {
      settings[it.getId()] = fullWhitelist[it.getId()]?.let { ps ->
        WhitelistSettingsData(ps)
      } ?: WhitelistSettingsData()
    }

    val filtered = fullList.drop(1).filter {
      settings[it.getId()]?.canLaunch == true && (!onlyRunning || it.isRunning)
    }.toMutableList()

    intentSender.launchLastApp(filtered, startActivity)
  }

  private fun isOnlyRunning() = settingsHolder.getOnlyRunning()

  private fun hasPrivileges() = shizukuManager.isShizukuAllowed() || rootBeer.isRooted
}