package com.tymwitko.recents.kill

import com.tymwitko.recents.BuildConfig
import com.tymwitko.recents.common.EmptyError
import com.tymwitko.recents.common.Result
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
  suspend fun killAll(): Result<Int, EmptyError> {
    var killCount = 0
    return appsAccessor.getRecentApps(hasPrivileges())
      .filter { it.packageName != BuildConfig.APPLICATION_ID && it.isRunning }
      .let {
        it.collect { app ->
          if (killIndividualApp(app) is Result.Success) killCount++
        }
        if (killCount != 0) Result.Success(killCount) else Result.Failure(EmptyError())
      }
  }

  suspend fun killIndividualApp(app: App): Result<App, EmptyError> =
    try {
      appKiller.killApp(app)
      Result.Success(app)
    } catch (_: AppNotKilledException) {
      Result.Failure(EmptyError())
    }

  private fun hasPrivileges() = shizukuManager.isShizukuAllowed() || settingsHolder.hasRootAccess()
}
