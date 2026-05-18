package com.tymwitko.recents.settings.whitelist

import androidx.lifecycle.ViewModel
import com.tymwitko.recents.settings.SettingsHolder

class SettingsViewModel(private val uiSettingsHolder: SettingsHolder): ViewModel() {
  fun getFontSize() = uiSettingsHolder.getFontSize()
}