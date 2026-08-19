package com.tymwitko.recents.common.koin

import android.app.usage.UsageStatsManager
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.LauncherApps
import android.view.WindowManager
import androidx.room.Room
import com.tymwitko.recents.common.FetchAppsUseCase
import com.tymwitko.recents.common.FetchPinnedAppsUseCase
import com.tymwitko.recents.common.SHARED_PREFS_KEY
import com.tymwitko.recents.common.accessors.AppsAccessor
import com.tymwitko.recents.common.accessors.DumpyFetcher
import com.tymwitko.recents.common.accessors.IconAccessor
import com.tymwitko.recents.common.accessors.IntentSender
import com.tymwitko.recents.common.accessors.RootManager
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.common.db.Migrations
import com.tymwitko.recents.common.db.RecentsDao
import com.tymwitko.recents.common.db.RecentsDatabase
import com.tymwitko.recents.entry.EntryViewModel
import com.tymwitko.recents.kill.AppKiller
import com.tymwitko.recents.kill.KillAppsUseCase
import com.tymwitko.recents.lastapp.LastAppViewModel
import com.tymwitko.recents.lastapp.LaunchLastAppUseCase
import com.tymwitko.recents.recentapps.RecentAppsViewModel
import com.tymwitko.recents.recentapps.pinned.db.PinnedRepository
import com.tymwitko.recents.settings.SettingsHolder
import com.tymwitko.recents.settings.SettingsViewModel
import com.tymwitko.recents.settings.advanced.AdvancedSettingsViewModel
import com.tymwitko.recents.settings.pinned.PinnedViewModel
import com.tymwitko.recents.settings.ui.UiSettingsViewModel
import com.tymwitko.recents.settings.whitelist.WhitelistViewModel
import com.tymwitko.recents.settings.whitelist.db.WhitelistRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
 viewModel { LastAppViewModel(get()) }
  viewModel {
    RecentAppsViewModel(
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      androidContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager,
      get()
    )
  }
  single {
    IntentSender(
      androidContext().packageManager,
      androidContext().getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps,
      androidContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
    )
  }
  single {
    AppsAccessor(
      androidContext().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager,
      androidContext().packageManager,
      get(),
      androidContext().getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps,
      get()
    )
  }
  single {
    IconAccessor(
      androidContext().packageManager,
    )
  }
  singleOf(::AppKiller)
  single {
    Room.databaseBuilder(
      context = get(),
      klass = RecentsDatabase::class.java,
      name = "recents_db"
    )
      .addMigrations(Migrations.MIG_1_2)
      .addMigrations(Migrations.MIG_2_3)
      .addMigrations(Migrations.MIG_3_4)
      .build()
  }
  single<RecentsDao> {
    val db = get<RecentsDatabase>()
    db.recentsDao()
  }
  viewModel {
    WhitelistViewModel(
      get(),
      get(),
      get(),
      androidContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    )
  }
  singleOf(::WhitelistRepository)
  singleOf(::ShizukuManager)
  viewModel {
    UiSettingsViewModel(get(), get())
  }
  viewModelOf(::SettingsViewModel)
  singleOf(::SettingsHolder)
  single {
    SettingsHolder(
      androidContext().getSharedPreferences(
        SHARED_PREFS_KEY,
        Context.MODE_PRIVATE
      ),
      get()
    )
  }
  singleOf(::DumpyFetcher)
  viewModel { AdvancedSettingsViewModel(get(), get()) }
  singleOf(::PinnedRepository)
  viewModel {
    PinnedViewModel(
      get(),
      get(),
      get(),
      get(),
      androidContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    )
  }
  viewModelOf(::EntryViewModel)
  singleOf(::FetchAppsUseCase)
  singleOf(::KillAppsUseCase)
  singleOf(::LaunchLastAppUseCase)
  singleOf(::RootManager)
  singleOf(::FetchPinnedAppsUseCase)
}
