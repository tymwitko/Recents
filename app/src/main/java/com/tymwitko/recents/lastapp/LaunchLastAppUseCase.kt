package com.tymwitko.recents.lastapp

import android.content.Intent
import android.os.Bundle
import com.tymwitko.recents.common.accessors.AppsAccessor
import com.tymwitko.recents.common.accessors.IntentSender
import com.tymwitko.recents.common.accessors.RootManager
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.common.distinctByNamePickApp
import com.tymwitko.recents.settings.SettingsHolder
import com.tymwitko.recents.settings.whitelist.db.WhitelistRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList

class LaunchLastAppUseCase(
  private val appsAccessor: AppsAccessor,
  private val whitelistRepository: WhitelistRepository,
  private val settingsHolder: SettingsHolder,
  private val shizukuManager: ShizukuManager,
  private val rootManager: RootManager,
  private val intentSender: IntentSender
) {
  suspend operator fun invoke(
    thisPackageName: String,
    startActivity: (Intent, Bundle?) -> Unit
  ): Boolean = coroutineScope {
    val privileges = hasPrivileges()
    val onlyRunning = isOnlyRunning()

    val fullDeferred = async {
      (appsAccessor.getRecentApps(privileges, isQuick = true).toList()
        .takeIf { it.isNotEmpty() } ?: appsAccessor.getRecentApps(privileges).toList())
        .filter {
          it.packageName != thisPackageName
        }
        .distinctByNamePickApp()
        .sortedByDescending { it.lastTimeUsed }
        .toMutableList()
        .drop(1)
    }

    val whiteListDeferred = async {
      whitelistRepository.getAllEntries()
    }

    val fullList = fullDeferred.await()
    val fullWhitelist = whiteListDeferred.await()

    fullList.firstOrNull {
      !appsAccessor.isLauncher(it.packageName)
        && fullWhitelist[it.getId()]?.canLaunch != false
        && (!onlyRunning || it.isRunning)
        && intentSender.launchSelectedApp(it, startActivity)
    } != null
  }

  private fun isOnlyRunning() = settingsHolder.getOnlyRunning()

  private fun hasPrivileges() = shizukuManager.isShizukuAllowed() || rootManager.hasRoot()
}
