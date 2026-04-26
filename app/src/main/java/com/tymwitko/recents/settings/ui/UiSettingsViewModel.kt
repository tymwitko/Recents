package com.tymwitko.recents.settings.ui

import androidx.lifecycle.ViewModel
import com.scottyab.rootbeer.RootBeer
import com.tymwitko.recents.common.accessors.ShizukuManager

class UiSettingsViewModel(
  private val uiSettingsHolder: UiSettingsHolder,
  private val rootBeer: RootBeer,
  private val shizukuManager: ShizukuManager
): ViewModel() {
  fun saveFontSize(size: Float) {
    uiSettingsHolder.storeFontSize(size.toInt())
  }

  fun getFontSize() = uiSettingsHolder.getFontSize()
  
  fun saveIconSize(size: Float) {
    uiSettingsHolder.storeIconSize(size.toInt())
  }
  
  fun getIconSize(default: Int) = uiSettingsHolder.getIconSize(default)
  
  fun hasPrivileges() = shizukuManager.isShizukuAllowed() || rootBeer.isRooted
}