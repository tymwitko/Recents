package com.tymwitko.recents.koin

import com.tymwitko.recents.IntentSender
import com.tymwitko.recents.PermissionsViewModel
import com.tymwitko.recents.RecentAppsViewModel
import com.tymwitko.recents.RecentAppsAccessor
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { PermissionsViewModel() }
    viewModel { RecentAppsViewModel() }
    single { IntentSender() }
    single { RecentAppsAccessor() }
}