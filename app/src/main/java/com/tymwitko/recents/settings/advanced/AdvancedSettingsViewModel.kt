package com.tymwitko.recents.settings.advanced

import android.os.Build
import androidx.lifecycle.ViewModel
import com.scottyab.rootbeer.RootBeer
import com.tymwitko.recents.R
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.settings.SettingsHolder

class AdvancedSettingsViewModel(
  private val settingsHolder: SettingsHolder,
  private val rootBeer: RootBeer,
  private val shizukuManager: ShizukuManager
) : ViewModel() {
  fun saveOnlyRunning(onlyRunning: Boolean) = settingsHolder.storeOnlyRunning(onlyRunning)

  fun getOnlyRunning() = settingsHolder.getOnlyRunning()


  fun saveSwipeToDelete(onlyRunning: Boolean) = settingsHolder.storeSwipeToDelete(onlyRunning)

  fun isSwipeToDelete() = settingsHolder.getSwipeToDelete()
  
  fun canSetOnlyRunning() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
    (shizukuManager.isShizukuAllowed() || rootBeer.isRooted)
  
  fun getResourceStringForOption(isSwipeToDelete: Boolean) =
    if (isSwipeToDelete) R.string.swipe_option else R.string.button_option
}
