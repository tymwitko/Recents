package com.tymwitko.recents

import android.content.Context
import androidx.lifecycle.ViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RecentAppsViewModel: ViewModel(), KoinComponent {

    private val intentSender: IntentSender by inject()
    private val recentAppsAccessor: RecentAppsAccessor by inject()

    fun launchLastApp(context: Context) {
        recentAppsAccessor.getRecentAppsFormatted(context)
            ?.let { intentSender.launchLastApp(context, it) }
    }

    fun launchRecents() {

    }
}