package com.tymwitko.recents.koin

import com.tymwitko.recents.accessors.AppKiller
import com.tymwitko.recents.accessors.IconAccessor
import com.tymwitko.recents.accessors.IntentSender
import com.tymwitko.recents.accessors.RecentAppsAccessor
import com.tymwitko.recents.viewmodels.LastAppViewModel
import com.tymwitko.recents.viewmodels.RecentAppsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { LastAppViewModel() }
    viewModel { RecentAppsViewModel() }
    single { IntentSender() }
    single { RecentAppsAccessor() }
    single { IconAccessor() }
    single { AppKiller() }
}