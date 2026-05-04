package com.tymwitko.recents.common.koin

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.LauncherApps
import android.view.accessibility.AccessibilityManager
import androidx.room.Room
import com.scottyab.rootbeer.RootBeer
import com.tymwitko.recents.common.SHARED_PREFS_KEY
import com.tymwitko.recents.common.accessors.AppKiller
import com.tymwitko.recents.common.accessors.AppsAccessor
import com.tymwitko.recents.common.accessors.DumpyFetcher
import com.tymwitko.recents.common.accessors.IconAccessor
import com.tymwitko.recents.common.accessors.IntentSender
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.lastapp.LastAppViewModel
import com.tymwitko.recents.recentapps.RecentAppsViewModel
import com.tymwitko.recents.settings.ui.UiSettingsHolder
import com.tymwitko.recents.settings.ui.UiSettingsViewModel
import com.tymwitko.recents.settings.whitelist.SettingsViewModel
import com.tymwitko.recents.settings.whitelist.WhitelistViewModel
import com.tymwitko.recents.settings.whitelist.db.Migrations
import com.tymwitko.recents.settings.whitelist.db.RecentsDatabase
import com.tymwitko.recents.settings.whitelist.db.WhitelistDao
import com.tymwitko.recents.settings.whitelist.db.WhitelistRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
  viewModelOf(::LastAppViewModel)
  viewModelOf(::RecentAppsViewModel)
  single { RootBeer(androidContext()) }
  single { IntentSender(androidContext().packageManager, androidContext().getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps) }
  single {
    AppsAccessor(
      androidContext().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager,
      androidContext().packageManager,
      get(),
      get(),
      androidContext().getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps,
      get()
    )
  }
  single { IconAccessor(androidContext().packageManager) }
  single {
    AppKiller(
      androidContext().packageManager,
      androidContext().getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager,
      get(),
      get(),
      get()
    )
  }
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
  viewModelOf(::UiSettingsViewModel)
  viewModelOf(::SettingsViewModel)
  singleOf(::UiSettingsHolder)
  single {
    UiSettingsHolder(
    androidContext().getSharedPreferences(
        SHARED_PREFS_KEY,
        Context.MODE_PRIVATE
        )
    )
  }
  singleOf(::DumpyFetcher)
}
