package com.tymwitko.recents.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.tymwitko.recents.accessors.IntentSender
import com.tymwitko.recents.accessors.RecentAppsAccessor
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LastAppViewModel: ViewModel(), KoinComponent {

    private val intentSender: IntentSender by inject()
    private val recentAppsAccessor: RecentAppsAccessor by inject()

    fun launchLastApp(context: Context) {
        recentAppsAccessor.getRecentAppsFormatted(context)
            .drop(1)
            .filter { !recentAppsAccessor.isLauncher(it, context) }
            .let { intentSender.launchLastApp(context, it) }
    }
}