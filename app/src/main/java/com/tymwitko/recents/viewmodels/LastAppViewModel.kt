package com.tymwitko.recents.viewmodels

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.tymwitko.recents.accessors.IntentSender
import com.tymwitko.recents.accessors.RecentAppsAccessor
import com.tymwitko.recents.consts.Whitelist
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LastAppViewModel: ViewModel(), KoinComponent {

    private val intentSender: IntentSender by inject()
    private val recentAppsAccessor: RecentAppsAccessor by inject()

    fun launchLastApp(startActivity: (Intent) -> Unit, thisPackageName: String) {
        recentAppsAccessor.getRecentAppsFormatted(thisPackageName)
            .drop(1)
            .filter { !recentAppsAccessor.isLauncher(it) }
            .filter { !Whitelist.isWhitelistedAgainstLaunching(it) }
            .let { intentSender.launchLastApp(it, startActivity) }
    }
}