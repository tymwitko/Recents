package com.tymwitko.recents

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

class RecentAppsAccessor {

    fun getRecentAppsFormatted(context: Context) = getRecentApps(context)
        ?.sortedBy { it.lastTimeUsed }
        ?.map { it.packageName }
        ?.filter { it != THIS_APP }
        ?.dropLast(1)
        ?.filter { !isLauncher(it, context) }
        ?.reversed()

    private fun isLauncher(packageName: String, context: Context): Boolean {
        val localPackageManager: PackageManager = context.packageManager
        val intent = Intent("android.intent.action.MAIN")
        intent.addCategory("android.intent.category.HOME")
        val str = localPackageManager
            .resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName
        return packageName == str
    }

    private fun getRecentApps(context: Context): MutableList<UsageStats>? {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 1000 * 60 * 60 * 24 // for last 24 hours stats

        return usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            beginTime,
            endTime
        )
//            .map { "${it.packageName} time: ${(Instant.ofEpochMilli(it.lastTimeUsed).atZone(ZoneId.systemDefault()))}, timeForeground: ${Instant.ofEpochMilli(it.lastTimeForegroundServiceUsed).atZone(ZoneId.systemDefault())}" }
    }
}