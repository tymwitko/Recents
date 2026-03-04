package com.tymwitko.recents.common.koin

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.loadKoinModules

class RecentsKoinApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    GlobalContext.startKoin {
      androidContext(this@RecentsKoinApplication)
      appModule
    }
    loadKoinModules(appModule)
  }
}
