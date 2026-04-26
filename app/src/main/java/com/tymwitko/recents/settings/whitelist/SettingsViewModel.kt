package com.tymwitko.recents.settings.whitelist

import androidx.lifecycle.ViewModel
import com.tymwitko.recents.settings.ui.UiSettingsHolder

class SettingsViewModel(private val uiSettingsHolder: UiSettingsHolder): ViewModel() {
  fun getFontSize() = uiSettingsHolder.getFontSize()
}