package com.tymwitko.recents.koin

import android.app.usage.UsageStatsManager
import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.room.Room
import com.scottyab.rootbeer.RootBeer
import com.tymwitko.recents.accessors.AppKiller
import com.tymwitko.recents.accessors.AppsAccessor
import com.tymwitko.recents.accessors.IconAccessor
import com.tymwitko.recents.accessors.IntentSender
import com.tymwitko.recents.viewmodels.LastAppViewModel
import com.tymwitko.recents.viewmodels.RecentAppsViewModel
import com.tymwitko.recents.viewmodels.WhitelistViewModel
import com.tymwitko.recents.whitelist.RecentsDatabase
import com.tymwitko.recents.whitelist.WhitelistDao
import com.tymwitko.recents.whitelist.WhitelistRepository
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::LastAppViewModel)
    viewModel { RecentAppsViewModel(get(), get(), get(), RootBeer(androidContext()), get()) }
    single { IntentSender(androidContext().packageManager) }
    single {
        AppsAccessor(
            androidContext().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager,
            androidContext().packageManager,
            get()
        )
    }
    single { IconAccessor(androidContext().packageManager) }
    single { AppKiller(
        androidContext().packageManager,
        androidContext().getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager,
        get()
    ) }
    single {
        Room.databaseBuilder(
            androidApplication(),
            RecentsDatabase::class.java,
            "RecentsDB"
        )
    }
    single {
        Room.databaseBuilder(
            context = get(),
            klass = RecentsDatabase::class.java,
            name = "recents_db"
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    single<WhitelistDao> {
        val db = get<RecentsDatabase>()
        db.whitelistDao()
    }
    viewModelOf(::WhitelistViewModel)
    singleOf(::WhitelistRepository)
}
