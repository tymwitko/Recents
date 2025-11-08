package com.tymwitko.recents.accessors

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException

class RecentAppsAccessor(
    private val usageStatsManager: UsageStatsManager,
    private val packageManager: PackageManager
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
            } catch (e: NameNotFoundException) {
                null
            }
        }

    fun isLauncher(packageName: String): Boolean {
        val intent = Intent("android.intent.action.MAIN")
        intent.addCategory("android.intent.category.HOME")
        val str = packageManager
            .resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName
        return packageName == str
    }

    private fun getRecentApps(): MutableList<UsageStats>? {
        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 1000 * 60 * 60 * 24 // for last 24 hours stats

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
}