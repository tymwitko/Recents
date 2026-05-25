package com.tymwitko.recents.settings.ui

import androidx.lifecycle.ViewModel
import com.scottyab.rootbeer.RootBeer
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.settings.SettingsHolder

class UiSettingsViewModel(
  private val settingsHolder: SettingsHolder,
  private val rootBeer: RootBeer,
  private val shizukuManager: ShizukuManager
): ViewModel() {
  fun saveFontSize(size: Float) {
    settingsHolder.storeFontSize(size.toInt())
  }

  fun getFontSize() = settingsHolder.getFontSize()
  
  fun saveIconSize(size: Float) {
    settingsHolder.storeIconSize(size.toInt())
  }
  
  fun getIconSize(default: Int) = settingsHolder.getIconSize(default)
  
  fun hasPrivileges() = shizukuManager.isShizukuAllowed() || rootBeer.isRooted
  
  fun isSwipeToKill() = settingsHolder.getSwipeToDelete()
}