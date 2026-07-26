package com.tymwitko.recents.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tymwitko.recents.common.accessors.RootManager
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.settings.SettingsHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UiSettingsViewModel(
  private val settingsHolder: SettingsHolder,
  private val rootManager: RootManager,
  private val shizukuManager: ShizukuManager
) : ViewModel() {

  private val _hasPrivileges = MutableStateFlow(false)
  val hasPrivileges: StateFlow<Boolean>
    get() = _hasPrivileges

  fun saveFontSize(size: Float) {
    settingsHolder.storeFontSize(size.toInt())
  }

  fun getFontSize() = settingsHolder.getFontSize()

  fun saveIconSize(size: Float) {
    settingsHolder.storeIconSize(size.toInt())
  }

  fun getIconSize(default: Int) = settingsHolder.getIconSize(default)

  fun checkPrivileges() {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        _hasPrivileges.update {
          shizukuManager.isShizukuAllowed() || rootManager.hasRoot()
        }
      }
    }
  }

  fun isSwipeToKill() = settingsHolder.getSwipeToDelete()

  fun getMarginSize() = settingsHolder.getMarginSize()

  fun saveMarginSize(size: Float) {
    settingsHolder.storeMarginSize(size.toInt())
  }

  fun toggleOrder(isReversed: Boolean) {
    settingsHolder.storeOrder(isReversed)
  }

  fun isOrderReversed() = settingsHolder.isOrderReversed()
}
