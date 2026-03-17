package com.tymwitko.recents.common.koin

import android.app.usage.UsageStatsManager
import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.room.Room
import com.scottyab.rootbeer.RootBeer
import com.tymwitko.recents.common.accessors.AppKiller
import com.tymwitko.recents.common.accessors.AppsAccessor
import com.tymwitko.recents.common.accessors.IconAccessor
import com.tymwitko.recents.common.accessors.IntentSender
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.lastapp.LastAppViewModel
import com.tymwitko.recents.recentapps.RecentAppsViewModel
import com.tymwitko.recents.whitelist.WhitelistViewModel
import com.tymwitko.recents.whitelist.db.Migrations
import com.tymwitko.recents.whitelist.db.RecentsDatabase
import com.tymwitko.recents.whitelist.db.WhitelistDao
import com.tymwitko.recents.whitelist.db.WhitelistRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::LastAppViewModel)
    viewModelOf(::RecentAppsViewModel)
    single { RootBeer(androidContext()) }
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
        get(),
        get(),
        get(),
        get()
    ) }
    single {
        Room.databaseBuilder(
            context = get(),
            klass = RecentsDatabase::class.java,
            name = "recents_db"
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .addMigrations(Migrations.MIG_1_2)
            .build()
    }

    single<WhitelistDao> {
        val db = get<RecentsDatabase>()
        db.whitelistDao()
    }
    viewModelOf(::WhitelistViewModel)
    singleOf(::WhitelistRepository)
    singleOf(::ShizukuManager)
}
