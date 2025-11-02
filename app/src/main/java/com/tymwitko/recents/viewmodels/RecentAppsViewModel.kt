package com.tymwitko.recents.viewmodels

import android.content.Intent
import android.content.pm.PackageInfo
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import com.scottyab.rootbeer.RootBeer
import com.tymwitko.recents.accessors.AppKiller
import com.tymwitko.recents.accessors.IconAccessor
import com.tymwitko.recents.accessors.IntentSender
import com.tymwitko.recents.accessors.RecentAppsAccessor
import com.tymwitko.recents.dataclasses.App
import com.tymwitko.recents.exceptions.EmptyAppListException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class RecentAppsViewModel(
    private val recentAppsAccessor: RecentAppsAccessor,
    private val appKiller: AppKiller,
    private val iconAccessor: IconAccessor,
    private val rootBeer: RootBeer,
    private val intentSender: IntentSender
): ViewModel(), KoinComponent {

    private fun getActivePackages(packageName: String) =
        recentAppsAccessor.getRecentAppsFormatted(packageName)
            .filter { !recentAppsAccessor.isLauncher(it) }
            .toSet()
            .also {
                if (it.isEmpty()) {
                    Log.d("TAG", "List empty")
                    throw EmptyAppListException()
                }
                it.forEachIndexed { ind, t ->
                    Log.d("TAG", "App $ind is $t")
                }
            }

    fun getActiveApps(packageName: String, placeHolderIcon: ImageBitmap?) = getActivePackages(packageName).map {
        App(
            recentAppsAccessor.getAppName(it).orEmpty(),
            it,
            getAppIcon(it, placeHolderIcon)
        )
    }

    fun killEmAll() {
        recentAppsAccessor.getRecentsAsPackageInfos("") // todo
            .filter { pi ->
                !appKiller.hasAccessibilityService(pi.packageName) &&
                    !appKiller.hasSetAlarmPermission(pi.packageName)
            }
            .forEach { pi ->
                CoroutineScope(Dispatchers.IO).launch {
                    killByPackageInfo(pi)
                }
            }
    }

    suspend fun killByPackageInfo(packageInfo: PackageInfo) =
        appKiller.killByPackageInfo(packageInfo)

    fun hasRoot() = rootBeer.isRooted

    fun launchApp(packageName: String, startActivity: (Intent) -> Unit) =
        intentSender.launchSelectedApp(packageName, startActivity)

    private fun getAppIcon(packageName: String, placeHolderIcon: ImageBitmap?) =
        iconAccessor.getAppIcon(packageName) ?: placeHolderIcon
         ?: throw NoSuchElementException()
}