package com.tymwitko.recents

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context

class RecentAppsAccessor {

    fun getRecentAppsFormatted(context: Context) = getRecentApps(context)
        ?.sortedBy { it.lastTimeUsed }
        ?.map { it.packageName }
        ?.filter { it != THIS_APP }
        ?.dropLast(1)
        ?.filter { !isLauncher(it) }
        ?.reversed()

    private fun isLauncher(packageName: String) = LauncherPackageNames.let {
        listOf(it.M_LAUNCHER, it.OPEN_LAUNCHER, it.LUNAR_LAUNCHER, it.PIE_LAUNCHER, it.ONE_UI_LAUNCHER, it.LAWNCHAIR).contains(packageName)
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