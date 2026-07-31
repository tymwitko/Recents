package com.tymwitko.recents.common

import com.tymwitko.recents.BuildConfig
import com.tymwitko.recents.common.accessors.AppKiller
import com.tymwitko.recents.common.accessors.AppsAccessor
import com.tymwitko.recents.common.accessors.RootManager
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.common.dataclasses.App
import kotlinx.coroutines.flow.filter

class KillAppsUseCase(
  private val appKiller: AppKiller,
  private val appsAccessor: AppsAccessor,
  private val shizukuManager: ShizukuManager,
  private val rootManager: RootManager
) {
  suspend fun killAll(): Boolean {
    var killCount = 0
    return appsAccessor.getRecentApps(hasPrivileges())
      .filter { it.packageName != BuildConfig.APPLICATION_ID && it.isRunning }
      .let {
        it.collect { app ->
          if (killIndividualApp(app)) killCount++
        }
        killCount != 0
      }
  }

  suspend fun killIndividualApp(app: App) =
    runCatching {
      appKiller.killApp(app)
      true
    }.getOrDefault(false)

  private fun hasPrivileges() = shizukuManager.isShizukuAllowed() || rootManager.hasRoot()
}
