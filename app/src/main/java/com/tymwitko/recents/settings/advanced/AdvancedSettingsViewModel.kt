package com.tymwitko.recents.settings.advanced

import androidx.lifecycle.ViewModel
import com.tymwitko.recents.settings.SettingsHolder

class AdvancedSettingsViewModel(private val settingsHolder: SettingsHolder) : ViewModel() {
  fun saveOnlyRunning(onlyRunning: Boolean) = settingsHolder.storeOnlyRunning(onlyRunning)
  fun getOnlyRunning() = settingsHolder.getOnlyRunning()
}