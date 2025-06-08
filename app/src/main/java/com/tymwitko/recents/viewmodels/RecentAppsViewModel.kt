package com.tymwitko.recents.viewmodels

import android.content.Context
import android.content.pm.PackageInfo
import android.util.Log
import androidx.lifecycle.ViewModel
import com.tymwitko.recents.accessors.AppKiller
import com.tymwitko.recents.accessors.RecentAppsAccessor
import com.tymwitko.recents.dataclasses.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RecentAppsViewModel: ViewModel(), KoinComponent {

    private val recentAppsAccessor: RecentAppsAccessor by inject()
    private val appKiller: AppKiller by inject()

    private fun getActivePackages(context: Context) =
        recentAppsAccessor.getRecentAppsFormatted(context)
            .filter { !recentAppsAccessor.isLauncher(it, context) }
            .toSet()
            .also {
                if (it.isEmpty()) Log.d("TAG", "List empty")
                it.forEachIndexed { ind, t ->
                    Log.d("TAG", "App $ind is $t")
                }
            }

    fun getActiveApps(context: Context) = getActivePackages(context).map {
        App(recentAppsAccessor.getAppName(it, context).orEmpty(), it)
    }

    fun killEmAll(context: Context?) {
        context?.let {
            recentAppsAccessor.getRecentsAsPackageInfos(it)
                .filter { pi ->
                    !appKiller.hasAccessibilityService(pi.packageName, context) &&
                        !appKiller.hasSetAlarmPermission(context, pi.packageName)
                }
                .forEach { pi ->
                    CoroutineScope(Dispatchers.IO).launch {
                        killByPackageInfo(pi)
                    }
                }
        }
    }

    suspend fun killByPackageInfo(packageInfo: PackageInfo) =
        appKiller.killByPackageInfo(packageInfo)
}