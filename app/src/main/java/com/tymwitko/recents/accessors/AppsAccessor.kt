package com.tymwitko.recents.accessors

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import com.tymwitko.recents.whitelist.WhitelistRepository

class AppsAccessor(
  private val usageStatsManager: UsageStatsManager,
  private val packageManager: PackageManager,
  private val whitelistRepository: WhitelistRepository,
  private val systemAppsVisibilityManager: SystemAppsVisibilityManager
) {

  fun getRecentAppsFormatted(thisPackageName: String, shouldShowSystemApps: Boolean? = null) = getRecentApps()
    ?.sortedBy { it.lastTimeUsed }
    ?.filter {
      (shouldShowSystemApps ?: (systemAppsVisibilityManager.shouldShowSystemApps() != false))
        || !isSystemApp(packageManager.getPackageInfo(it.packageName, PackageManager.GET_META_DATA))
    }
    ?.map { it.packageName }
    ?.filter { it != thisPackageName }
    ?.reversed()
    .orEmpty()

  fun getAppsAlphabeticaly(thisPackageName: String) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      packageManager.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
    } else {
      packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
    }

  fun getRecentsAsPackageInfos(thisPackageName: String) =
    getRecentAppsFormatted(thisPackageName, true).mapNotNull {
      try {
        packageManager.getPackageInfo(it, PackageManager.GET_META_DATA)
      } catch (e: NameNotFoundException) {
        null
      }
    }

  suspend fun shouldLaunch(packageName: String) = isLauncher(packageName) && canLaunch(packageName)

  fun isLauncher(packageName: String): Boolean {
    val intent = Intent("android.intent.action.MAIN")
    intent.addCategory("android.intent.category.HOME")
    val str = packageManager
      .resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName
    return packageName == str
  }

  private fun getRecentApps(): MutableList<UsageStats>? {
    val endTime = System.currentTimeMillis()
    val beginTime = endTime - 1000 * 60 * 60 * 24 // stats from the last 24 hours

    return usageStatsManager.queryUsageStats(
      UsageStatsManager.INTERVAL_DAILY,
      beginTime,
      endTime
    )
  }

  fun getAppName(packageName: String): String? {
    val appInfo = try {
      packageManager.getApplicationInfo(packageName, 0)
    } catch (e: NameNotFoundException) {
      null
    }
    return appInfo?.let { packageManager.getApplicationLabel(appInfo).toString() }
  }

  private suspend fun canLaunch(packageName: String) = whitelistRepository.canLaunch(packageName)

  private fun isSystemApp(packageInfo: PackageInfo): Boolean =
    ApplicationInfo.FLAG_SYSTEM and (packageInfo.applicationInfo?.flags ?: 0) != 0
}
