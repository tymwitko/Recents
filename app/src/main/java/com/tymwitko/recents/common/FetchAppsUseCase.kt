package com.tymwitko.recents.common

import com.tymwitko.recents.common.accessors.AppsAccessor
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
  private val settingsHolder: SettingsHolder
) {
  suspend operator fun invoke(
    thisPackageName: String,
    hasPrivileges: Boolean,
    isOnlyRunning: Boolean = false
  ): AllAppsData {
    val settings = mutableMapOf<String, WhitelistSettingsData>()
    val fullList = appsAccessor.getRecentApps(hasPrivileges).toList()
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
    val filtered = fullList.filter {
      whitelistRepository.canShow(it.getId()) && (!isOnlyRunning() || it.isRunning)
    }.toMutableList()
    val pinned = pinnedRepository.getAllPinned()
    val pinnedApps =
      fullList.filter {
        PinnedAppDetails(it) in pinned
      }.toMutableList()
    return AllAppsData(fullList, filtered, pinnedApps, settings)
  }

  private fun isOnlyRunning() = settingsHolder.getOnlyRunning()
}
