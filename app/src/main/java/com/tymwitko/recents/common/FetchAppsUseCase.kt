package com.tymwitko.recents.common

import com.scottyab.rootbeer.RootBeer
import com.tymwitko.recents.common.accessors.AppsAccessor
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.recentapps.pinned.db.PinnedAppDetails
import com.tymwitko.recents.recentapps.pinned.db.PinnedRepository
import com.tymwitko.recents.settings.SettingsHolder
import com.tymwitko.recents.settings.whitelist.WhitelistSettingsData
import com.tymwitko.recents.settings.whitelist.db.WhitelistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext

class FetchAppsUseCase(
  private val appsAccessor: AppsAccessor,
  private val whitelistRepository: WhitelistRepository,
  private val pinnedRepository: PinnedRepository,
  private val settingsHolder: SettingsHolder,
  private val shizukuManager: ShizukuManager,
  private val rootBeer: RootBeer
) {
  suspend operator fun invoke(
    thisPackageName: String,
    withFilters: Boolean,
    isOnlyRunning: Boolean = false
  ): AllAppsData {
    val settings = mutableMapOf<String, WhitelistSettingsData>()
    val privileges = hasPrivileges()
    val fullList = appsAccessor.getRecentApps(privileges).toList()
      .filter {
        it.packageName != thisPackageName && !appsAccessor.isLauncher(it.packageName)
      }
      .distinctByNamePickApp()
      .sortedByDescending { it.lastTimeUsed }
      .toMutableList()
      .onEach {
        withContext(Dispatchers.IO) {
          settings[it.getId()] = WhitelistSettingsData()
          whitelistRepository.getEntry(it.getId())?.let { packageSettings ->
            settings[it.getId()] =
              WhitelistSettingsData(
                packageSettings.canLaunch,
                packageSettings.canKill,
                packageSettings.canShow
              )
          }
        }
      }
    var filtered = listOf<App>()
    var pinnedApps = listOf<App>()
    val onlyRunning = isOnlyRunning()
    if (withFilters) {
      filtered = fullList.filter {
        whitelistRepository.canShow(it.getId()) && (!onlyRunning || it.isRunning)
      }.toMutableList()
      val pinned = pinnedRepository.getAllPinned()
      pinnedApps =
        fullList.filter {
          PinnedAppDetails(it) in pinned
        }.toMutableList()
    }
    return AllAppsData(
      fullList,
      filtered,
      pinnedApps,
      settings,
      privileges,
      onlyRunning
    )
  }

  private fun isOnlyRunning() = settingsHolder.getOnlyRunning()

  private fun hasPrivileges() = shizukuManager.isShizukuAllowed() || rootBeer.isRooted
}
