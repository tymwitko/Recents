package com.tymwitko.recents.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.tymwitko.recents.dataclasses.App
import com.tymwitko.recents.accessors.RecentAppsAccessor
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RecentAppsViewModel: ViewModel(), KoinComponent {

    private val recentAppsAccessor: RecentAppsAccessor by inject()

    private fun getActivePackages(context: Context) = recentAppsAccessor.getRecentAppsFormatted(context)
        .also {
            if (it.isEmpty()) Log.d("TAG", "List empty")
            it.forEachIndexed { ind, t ->
                Log.d("TAG", "App $ind is $t")
            }
        }

    fun getActiveApps(context: Context) = getActivePackages(context).map {
        App(recentAppsAccessor.getAppName(it, context).orEmpty(), it)
    }
}