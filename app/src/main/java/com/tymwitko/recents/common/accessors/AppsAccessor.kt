package com.tymwitko.recents.common.accessors

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import android.os.UserHandle
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.ImageBitmap
import com.tymwitko.recents.common.MILLIS_IN_DAY
import com.tymwitko.recents.common.MILLIS_IN_HOUR
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.common.dataclasses.DumpApp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

class AppsAccessor(
  context: Context,
  private val dumpyFetcher: DumpyFetcher,
  private val iconAccessor: IconAccessor
) {

  private val usageStatsManager =
    context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
  private val packageManager = context.packageManager
  private val launcherApps =
    context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

  suspend fun getRecentApps(
    hasPrivileges: Boolean,
    isOnlyRunning: Boolean = false,
    isQuick: Boolean = false
  ): Flow<App> = coroutineScope {
    (
      when {
        isOnlyRunning && hasPrivileges && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
          getRunningApps()
        !isOnlyRunning && hasPrivileges && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
          getLauncherActivityList()
        else -> getRecentAppsFormatted(isQuick)
      }
    ).asFlow()
  }

  private fun getRecentAppsFormatted(isQuick: Boolean): List<App> {
    val runningApps = runCatching { dumpyFetcher.getRunningPackages() }.getOrNull()
    return getAppsViaUsageStatsManager(isQuick)
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
  private suspend fun getRunningApps(): List<App> {
    val launcherActivities = getAllLauncherActivities()
    return dumpyFetcher.getRunningPackages()
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

  fun isLauncher(packageName: String): Boolean {
    val intent = Intent("android.intent.action.MAIN")
    intent.addCategory("android.intent.category.HOME")
    val str = packageManager
      .resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName
    return packageName == str
  }

  private fun getAppsViaUsageStatsManager(isQuick: Boolean): MutableList<UsageStats>? {
    val endTime = System.currentTimeMillis()
    val beginTime = endTime - if (isQuick) MILLIS_IN_HOUR else MILLIS_IN_DAY

    return usageStatsManager.queryUsageStats(
      UsageStatsManager.INTERVAL_DAILY,
      beginTime,
      endTime
    )
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private suspend fun getLauncherActivityList(): List<App> {
    val runningApps = dumpyFetcher.getRunningPackages()
    return getAllLauncherActivities()
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
      .applyTime()
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

  private fun List<DumpApp>.applyTime(): List<App> {
    val timestamps = dumpyFetcher.getLastUsesViaDumpsys()
    return this.map { app ->
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
