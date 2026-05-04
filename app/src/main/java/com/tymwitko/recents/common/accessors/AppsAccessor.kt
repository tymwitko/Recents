package com.tymwitko.recents.common.accessors

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.common.dataclasses.DumpApp
import com.tymwitko.recents.settings.whitelist.db.WhitelistRepository
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

class AppsAccessor(
  private val usageStatsManager: UsageStatsManager,
  private val packageManager: PackageManager,
  private val whitelistRepository: WhitelistRepository,
  private val dumpyFetcher: DumpyFetcher,
  private val launcherApps: LauncherApps,
  private val iconAccessor: IconAccessor
) {
  
  suspend fun getRecentApps(thisPackageName: String, isDumpsys: Boolean) =
    if (isDumpsys && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
      getFastActivityList()
    else getRecentAppsFormatted(thisPackageName)

  private fun getRecentAppsFormatted(thisPackageName: String) = getAppsViaUsageStatsManager()
    ?.sortedBy { it.lastTimeUsed }
    ?.filter { it.packageName != thisPackageName }
    ?.map {
      App(
        getAppName(it.packageName).orEmpty(),
        it.packageName,
        iconAccessor.getAppIcon(it.packageName)
      )
    }
    ?.reversed()
    .orEmpty()

  fun getRecentsAsPackageInfos(thisPackageName: String) =
    getRecentAppsFormatted(thisPackageName).mapNotNull {
      try {
        packageManager.getPackageInfo(it.packageName, PackageManager.GET_META_DATA)
      } catch (_: NameNotFoundException) {
        null
      }
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
  suspend fun getFastActivityList(): List<App> = 
    launcherApps.profiles.flatMap { userHandle ->
      currentCoroutineContext().ensureActive()
      launcherApps.getActivityList(null, userHandle).map { launcherActivityInfo ->
        currentCoroutineContext().ensureActive()
        Log.d("TAG", "asdf ${launcherActivityInfo.componentName}")
        launcherActivityInfo
      }
    }
  .map {
    DumpApp(
      it.label.toString(),
      it.applicationInfo.packageName,
      iconAccessor.getAppIcon(it.applicationInfo.packageName),
      it.componentName
    )
  }.distinctBy { it.packageName }.let(::sortByTime)

  fun getAppName(packageName: String): String? = getAppInfo(packageName)?.let { appInfo ->
    packageManager.getApplicationLabel(appInfo).toString()
  }
  
  fun isSystemApp(packageName: String) = isSystemApp(getAppInfo(packageName))
  
  fun isSystemApp(applicationInfo: ApplicationInfo?) =
    ApplicationInfo.FLAG_SYSTEM and (applicationInfo?.flags ?: 0) != 0

  private suspend fun canLaunch(packageName: String) = whitelistRepository.canLaunch(packageName)
  
  private fun getAppInfo(packageName: String) = try {
    packageManager.getApplicationInfo(packageName, 0)
  } catch (_: NameNotFoundException) {
    null
  }
  
  private fun sortByTime(appList: List<App>): List<App> {
    val timestamps = dumpyFetcher.getLastUsesViaDumpsys()
    return appList.sortedBy {
      timestamps[it.packageName]
    }.reversed()
  }
}
