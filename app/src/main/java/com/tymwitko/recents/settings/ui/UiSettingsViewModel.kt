package com.tymwitko.recents.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.settings.SettingsHolder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UiSettingsViewModel(
  private val settingsHolder: SettingsHolder,
  private val shizukuManager: ShizukuManager,
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

  val hasPrivileges: StateFlow<Boolean>
    field = MutableStateFlow(false)

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
      withContext(dispatcher) {
        hasPrivileges.update {
          shizukuManager.isShizukuAllowed() || settingsHolder.hasRootAccess()
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
