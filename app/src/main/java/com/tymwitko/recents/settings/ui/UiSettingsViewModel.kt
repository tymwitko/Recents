package com.tymwitko.recents.settings.ui

import androidx.lifecycle.ViewModel

class UiSettingsViewModel(private val uiSettingsHolder: UiSettingsHolder): ViewModel() {
  fun saveFontSize(size: Float) {
    uiSettingsHolder.storeFontSize(size.toInt())
  }
  fun getFontSize() = uiSettingsHolder.getFontSize()
}