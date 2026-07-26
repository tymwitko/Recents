package com.tymwitko.recents.common

import com.tymwitko.recents.common.accessors.AppKiller
import com.tymwitko.recents.common.accessors.AppsAccessor
import com.tymwitko.recents.common.accessors.RootManager
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.common.dataclasses.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.withContext

class KillAppsUseCase(
  private val appKiller: AppKiller,
  private val appsAccessor: AppsAccessor,
  private val shizukuManager: ShizukuManager,
  private val rootManager: RootManager
) {
  suspend fun killAll(
    thisPackageName: String
  ) = withContext(Dispatchers.IO) {
    var killCount = 0
    appsAccessor.getRecentApps(hasPrivileges())
      .filter { it.packageName != thisPackageName && it.isRunning }
      .let {
        it.collect { app ->
          if (killIndividualApp(app)) killCount++
        }
        killCount != 0
      }
  }

  suspend fun killIndividualApp(app: App) =
    runCatching {
      withContext(Dispatchers.IO) {
        appKiller.killApp(app)
        true
      }
    }.getOrDefault(false)

  private suspend fun hasPrivileges() = shizukuManager.isShizukuAllowed() || rootManager.hasRoot()
}
