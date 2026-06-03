package com.tymwitko.recents.entry

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.tymwitko.recents.lastapp.LastAppActivity
import com.tymwitko.recents.recentapps.RecentAppsActivity
import com.tymwitko.recents.settings.SettingsHolder

class EntryViewModel(private val settingsHolder: SettingsHolder) : ViewModel() {
  fun launchDefault(startActivity: (Class<out AppCompatActivity>) -> Unit) {
    startActivity(
      if (settingsHolder.isRecentsDefault()) RecentAppsActivity::class.java
      else LastAppActivity::class.java
    )
  }
}
