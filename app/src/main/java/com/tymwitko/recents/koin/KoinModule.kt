package com.tymwitko.recents.koin

import android.app.usage.UsageStatsManager
import android.content.Context
import android.view.accessibility.AccessibilityManager
import com.scottyab.rootbeer.RootBeer
import com.tymwitko.recents.accessors.AppKiller
import com.tymwitko.recents.accessors.IconAccessor
import com.tymwitko.recents.accessors.IntentSender
import com.tymwitko.recents.accessors.RecentAppsAccessor
import com.tymwitko.recents.viewmodels.LastAppViewModel
import com.tymwitko.recents.viewmodels.RecentAppsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { LastAppViewModel() }
    viewModel { RecentAppsViewModel(get(), get(), get(), RootBeer(androidContext()), get())     }
    single { IntentSender(androidContext().packageManager) }
    single {
        RecentAppsAccessor(
            androidContext().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager,
            androidContext().packageManager
        )
    }
    single { IconAccessor(androidContext().packageManager) }
    single { AppKiller(
        androidContext().packageManager,
        androidContext().getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    ) }
}
