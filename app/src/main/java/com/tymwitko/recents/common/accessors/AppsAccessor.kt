package com.tymwitko.recents.common.accessors

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import android.os.UserHandle
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.common.dataclasses.DumpApp
import com.tymwitko.recents.settings.whitelist.db.WhitelistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.withContext

class AppsAccessor(
  private val usageStatsManager: UsageStatsManager,
  private val packageManager: PackageManager,
  private val whitelistRepository: WhitelistRepository,
  private val dumpyFetcher: DumpyFetcher,
  private val launcherApps: LauncherApps,
  private val iconAccessor: IconAccessor
) {

  suspend fun getRecentApps(isDumpsys: Boolean): Flow<App> = coroutineScope {
    if (isDumpsys && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val dumps = async { getFastActivityList() }
      val usages = async { getRecentAppsFormatted(isDumpsys) }
      val dumpsFlow = dumps.await().asFlow()
      val usagesFlow = usages.await().asFlow()
      merge(dumpsFlow, usagesFlow)
    } else getRecentAppsFormatted(isDumpsys).asFlow()
  }

  private suspend fun getRecentAppsFormatted(isDumpsys: Boolean) = 
    withContext(Dispatchers.IO) {
      val runningApps = if (isDumpsys) dumpyFetcher.getRunningPackages() else null
      getAppsViaUsageStatsManager()
        ?.map {
          App(
            getAppName(it.packageName).orEmpty(),
            it.packageName,
            iconAccessor.getAppIcon(it.packageName),
            it.lastTimeUsed,
            runningApps?.firstOrNull { app -> 
              it.packageName == app.packageName && !app.isWorkApp
            }?.isRunning ?: false
          )
        }
        .orEmpty()
    }

  suspend fun shouldLaunch(packageName: String) = !isLauncher(packageName) && canLaunch(packageName)

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
  private suspend fun getFastActivityList(): List<App> = 
    withContext(Dispatchers.IO) {
      val runningApps = dumpyFetcher.getRunningPackages()
      launcherApps.profiles.flatMap { userHandle ->
        currentCoroutineContext().ensureActive()
        launcherApps.getActivityList(null, userHandle)
          .map { launcherActivityInfo ->
            currentCoroutineContext().ensureActive()
            launcherActivityInfo
          }
      }
        .map {
          DumpApp(
            it.label.toString(),
            it.applicationInfo.packageName,
            iconAccessor.getAppIcon(it.applicationInfo.packageName)
              ?: it.getBadgedIcon(0).toBitmap().asImageBitmap(),
            null,
            runningApps.firstOrNull { app -> 
              it.applicationInfo.packageName == app.packageName &&
                isSameUser(it.user, app.isWorkApp)
            }?.isRunning ?: false,
            it.componentName,
            it.user != launcherApps.profiles.first()
          )
        }.distinctBy { it.packageName }
        .let {
          applyTime(it)
        }
    }

  fun getAppName(packageName: String): String? = getAppInfo(packageName)?.let { appInfo ->
    packageManager.getApplicationLabel(appInfo).toString()
  }
  
  fun isSystemApp(packageName: String) = isSystemApp(getAppInfo(packageName))
  
  fun isSystemApp(applicationInfo: ApplicationInfo?) =
    ApplicationInfo.FLAG_SYSTEM and (applicationInfo?.flags ?: 0) != 0

  private suspend fun canLaunch(packageName: String) = whitelistRepository.canLaunch(packageName)
  
  @RequiresApi(Build.VERSION_CODES.O)
  private fun isSameUser(userHandle: UserHandle, isWorkApp: Boolean) =
    (userHandle == launcherApps.profiles.first()) == isWorkApp
  
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
}
