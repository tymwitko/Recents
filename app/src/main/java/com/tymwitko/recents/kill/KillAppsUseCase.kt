package com.tymwitko.recents.kill

import com.tymwitko.recents.BuildConfig
import com.tymwitko.recents.common.accessors.AppsAccessor
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.settings.SettingsHolder
import kotlinx.coroutines.flow.filter

class KillAppsUseCase(
  private val appKiller: AppKiller,
  private val appsAccessor: AppsAccessor,
  private val shizukuManager: ShizukuManager,
  private val settingsHolder: SettingsHolder
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

  private fun hasPrivileges() = shizukuManager.isShizukuAllowed() || settingsHolder.hasRootAccess()
}
