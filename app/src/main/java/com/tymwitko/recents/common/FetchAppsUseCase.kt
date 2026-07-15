package com.tymwitko.recents.common

import com.scottyab.rootbeer.RootBeer
import com.tymwitko.recents.common.accessors.AppsAccessor
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.recentapps.pinned.db.PinnedAppDetails
import com.tymwitko.recents.recentapps.pinned.db.PinnedRepository
import com.tymwitko.recents.settings.SettingsHolder
import com.tymwitko.recents.settings.whitelist.WhitelistSettingsData
import com.tymwitko.recents.settings.whitelist.db.WhitelistRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList

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
    withFilter: Boolean,
    withPinned: Boolean,
    isOnlyRunning: Boolean = false
  ): AllAppsData = coroutineScope {
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

    val filteredDeferred = async {
      if (withFilter) {
        fullList.filter {
          whitelistRepository.canShow(it.getId()) && (!onlyRunning || it.isRunning)
        }.toMutableList()
      } else return@async mutableListOf()
    }

    val pinnedDeferred = async {
      if (withPinned) {
        val pinned = pinnedRepository.getAllPinned()
        fullList.filter {
          PinnedAppDetails(it) in pinned
        }.toMutableList()
      } else return@async mutableListOf()
    }

    val filtered = filteredDeferred.await()
    val pinnedApps = pinnedDeferred.await()

    AllAppsData(
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
