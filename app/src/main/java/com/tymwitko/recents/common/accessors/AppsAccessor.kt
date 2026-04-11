package com.tymwitko.recents.common.accessors

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import com.tymwitko.recents.settings.whitelist.db.WhitelistRepository

class AppsAccessor(
  private val usageStatsManager: UsageStatsManager,
  private val packageManager: PackageManager,
  private val whitelistRepository: WhitelistRepository
) {

  fun getRecentAppsFormatted(thisPackageName: String) = getRecentApps()
    ?.sortedBy { it.lastTimeUsed }
    ?.map { it.packageName }
    ?.filter { it != thisPackageName }
    ?.reversed()
    .orEmpty()

  fun getRecentsAsPackageInfos(thisPackageName: String) =
    getRecentAppsFormatted(thisPackageName).mapNotNull {
      try {
        packageManager.getPackageInfo(it, PackageManager.GET_META_DATA)
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

  private fun getRecentApps(): MutableList<UsageStats>? {
    val endTime = System.currentTimeMillis()
    val beginTime = endTime - 1000 * 60 * 60 * 24 // stats from the last 24 hours

    return usageStatsManager.queryUsageStats(
      UsageStatsManager.INTERVAL_DAILY,
      beginTime,
      endTime
    )
  }

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
}
