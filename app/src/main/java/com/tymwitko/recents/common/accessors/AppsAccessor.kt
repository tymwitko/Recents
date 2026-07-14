package com.tymwitko.recents.common.accessors

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import android.os.UserHandle
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.ImageBitmap
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.common.dataclasses.DumpApp
import com.tymwitko.recents.settings.whitelist.db.WhitelistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.withContext

class AppsAccessor(
  private val usageStatsManager: UsageStatsManager,
  private val packageManager: PackageManager,
  private val whitelistRepository: WhitelistRepository,
  private val dumpyFetcher: DumpyFetcher,
  private val launcherApps: LauncherApps,
  private val iconAccessor: IconAccessor
) {

  suspend fun getRecentApps(
    hasPrivileges: Boolean,
    isOnlyRunning: Boolean = false
  ): Flow<App> = coroutineScope {
    (
      when {
        isOnlyRunning && hasPrivileges && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
          getRunningApps()
        !isOnlyRunning && hasPrivileges && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
          getLauncherActivityList()
        else -> getRecentAppsFormatted()
      }
    ).asFlow()
  }

  private suspend fun getRecentAppsFormatted() =
    withContext(Dispatchers.IO) {
      val runningApps = runCatching { dumpyFetcher.getRunningPackages() }.getOrNull()
      getAppsViaUsageStatsManager()
        ?.map {
          App(
            name = getAppName(it.packageName).orEmpty(),
            packageName = it.packageName,
            icon = iconAccessor.getAppIcon(it.packageName),
            lastTimeUsed = it.lastTimeUsed,
            isRunning = runningApps?.firstOrNull { app ->
              it.packageName == app.packageName && !app.isWorkApp
            }?.isRunning ?: false,
            isWorkApp = false
          )
        }
        .orEmpty()
    }

  @RequiresApi(Build.VERSION_CODES.O)
  private suspend fun getRunningApps(): List<App> =
    withContext(Dispatchers.IO) {
      val launcherActivities = getAllLauncherActivities()
      dumpyFetcher.getRunningPackages()
        .map {
          var name: String?
          var icon: ImageBitmap?
          if (!it.isWorkApp) {
            name = getAppName(it.packageName)
            icon = iconAccessor.getAppIcon(it.packageName)
          } else {
            getWorkAppNameAndIcon(it.packageName, launcherActivities).let {
              name = it?.first?.toString()
              icon = it?.second
            }           
          }
          DumpApp(
            name.orEmpty(),
            it.packageName,
            icon,
            it.lastActive,
            isRunning = true,
            it.componentName,
            it.isWorkApp
          )
        }
    }

  suspend fun shouldLaunch(app: App) =
    !isLauncher(app.packageName) && whitelistRepository.canLaunch(app.getId())

  fun isLauncher(packageName: String): Boolean {
    val intent = Intent("android.intent.action.MAIN")
    intent.addCategory("android.intent.category.HOME")
    val str = packageManager
      .resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName
    return packageName == str
  }

  private fun getAppsViaUsageStatsManager(): MutableList<UsageStats>? { val endTime = System.currentTimeMillis()
    val beginTime = endTime - 1000 * 60 * 60 * 24 // stats from the last 24 hours

    return usageStatsManager.queryUsageStats(
      UsageStatsManager.INTERVAL_DAILY,
      beginTime,
      endTime
    )
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private suspend fun getLauncherActivityList(): List<App> =
    withContext(Dispatchers.IO) {
      val runningApps = dumpyFetcher.getRunningPackages()
      getAllLauncherActivities()
        .map {
          val isWorkApp = it.user != launcherApps.profiles.first()
          DumpApp(
            it.label.toString(),
            it.applicationInfo.packageName,
            if (!isWorkApp)
              iconAccessor.getAppIcon(it.applicationInfo.packageName)
              else iconAccessor.getAppIconForWorkApp(it),
            null,
            runningApps.firstOrNull { app ->
              it.applicationInfo.packageName == app.packageName &&
                isSameUser(it.user, app.isWorkApp)
            }?.isRunning ?: false,
            it.componentName,
            isWorkApp
          )
        }
        .distinctBy { it.getId() }
        .let {
          applyTime(it)
        }
    }

  fun getAppName(packageName: String): String? = getAppInfo(packageName)?.let { appInfo ->
    packageManager.getApplicationLabel(appInfo).toString()
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun isSameUser(userHandle: UserHandle, isWorkApp: Boolean) =
    (userHandle == launcherApps.profiles.first()) != isWorkApp

  private fun getAppInfo(packageName: String) = try {
    packageManager.getApplicationInfo(packageName, 0)
  } catch (_: NameNotFoundException) {
    null
  }

  private fun applyTime(appList: List<DumpApp>): List<App> {
    val timestamps = dumpyFetcher.getLastUsesViaDumpsys()
    return appList.map { app ->
      app.copy(
        lastTimeUsed = timestamps[app.packageName]
      )
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private suspend fun getAllLauncherActivities() =
    launcherApps.profiles.flatMap { userHandle ->
      currentCoroutineContext().ensureActive()
      launcherApps.getActivityList(null, userHandle)
        .map { launcherActivityInfo ->
          currentCoroutineContext().ensureActive()
          launcherActivityInfo
        }
    }

  private fun getWorkAppNameAndIcon(
    packageName: String,
    launcherActivities: List<LauncherActivityInfo>
  ) = (
    launcherActivities.firstOrNull { it.applicationInfo.packageName == packageName }?.let {
      it.label to iconAccessor.getAppIconForWorkApp(it)
    }
  )
}
