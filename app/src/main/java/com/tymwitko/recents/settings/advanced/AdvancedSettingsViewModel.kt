package com.tymwitko.recents.settings.advanced

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tymwitko.recents.R
import com.tymwitko.recents.common.accessors.RootManager
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.settings.SettingsHolder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdvancedSettingsViewModel(
  private val settingsHolder: SettingsHolder,
  private val rootManager: RootManager,
  private val shizukuManager: ShizukuManager,
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
  private val _hasPrivileges = MutableStateFlow(false)
  val hasPrivileges: StateFlow<Boolean>
    get() = _hasPrivileges

  fun saveOnlyRunning(onlyRunning: Boolean) = settingsHolder.storeOnlyRunning(onlyRunning)

  fun getOnlyRunning() = settingsHolder.getOnlyRunning()

  fun saveSwipeToDelete(onlyRunning: Boolean) = settingsHolder.storeSwipeToDelete(onlyRunning)

  fun isSwipeToDelete() = settingsHolder.getSwipeToDelete()

  fun saveDefaultActivity(isRecents: Boolean) = settingsHolder.storeDefaultLauncher(isRecents)

  fun isRecentsDefault() = settingsHolder.isRecentsDefault()

  fun canSetOnlyRunning() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

  fun getResourceStringForKillOption(isSwipeToDelete: Boolean) =
    if (isSwipeToDelete) R.string.swipe_option else R.string.button_option

  fun getResourceStringForActivityOption(isRecents: Boolean) =
    if (isRecents) R.string.recents_option else R.string.last_app_option

  private fun hasPrivileges() = shizukuManager.isShizukuAllowed() || rootManager.hasRoot()

  fun checkPrivileges() {
    viewModelScope.launch {
      withContext(dispatcher) {
        _hasPrivileges.update { hasPrivileges() }
      }
    }
  }
}
