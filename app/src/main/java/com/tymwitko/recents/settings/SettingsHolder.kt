package com.tymwitko.recents.settings

import android.content.SharedPreferences
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.tymwitko.recents.common.DEFAULT_FONT_SIZE
import com.tymwitko.recents.common.DEFAULT_MARGIN_SIZE
import com.tymwitko.recents.common.FONT_SIZE_ALIAS
import com.tymwitko.recents.common.ICON_SIZE_ALIAS
import com.tymwitko.recents.common.IS_RECENTS_DEFAULT_ALIAS
import com.tymwitko.recents.common.IS_REVERSED_ORDER_ALIAS
import com.tymwitko.recents.common.MARGIN_SIZE_ALIAS
import com.tymwitko.recents.common.ONLY_RUNNING_ALIAS
import com.tymwitko.recents.common.ROOT_ACCESS_GRANTED_ALIAS
import com.tymwitko.recents.common.SWIPE_TO_DEL_ALIAS
import com.tymwitko.recents.common.accessors.RootManager

class SettingsHolder(
  private val sharedPrefs: SharedPreferences,
  private val rootManager: RootManager
) {
  fun storeFontSize(newSize: Int) {
    saveInt(FONT_SIZE_ALIAS, newSize)
  }

  fun getFontSize() = sharedPrefs.getInt(FONT_SIZE_ALIAS, DEFAULT_FONT_SIZE).sp

  fun storeIconSize(newSize: Int) {
    saveInt(ICON_SIZE_ALIAS, newSize)
  }

  fun getIconSize(defaultSize: Int) = sharedPrefs.getInt(ICON_SIZE_ALIAS, defaultSize).dp

  fun storeOnlyRunning(onlyRunning: Boolean) {
    saveBoolean(ONLY_RUNNING_ALIAS, onlyRunning)
  }

  fun getOnlyRunning() = sharedPrefs.getBoolean(ONLY_RUNNING_ALIAS, false)

  fun storeSwipeToDelete(swipeToDeleteOn: Boolean) {
    saveBoolean(SWIPE_TO_DEL_ALIAS, swipeToDeleteOn)
  }

  fun getSwipeToDelete() = sharedPrefs.getBoolean(SWIPE_TO_DEL_ALIAS, false)

  fun storeDefaultLauncher(isRecents: Boolean) {
    saveBoolean(IS_RECENTS_DEFAULT_ALIAS, isRecents)
  }

  fun isRecentsDefault() = sharedPrefs.getBoolean(IS_RECENTS_DEFAULT_ALIAS, true)

  fun storeMarginSize(size: Int) {
    saveInt(MARGIN_SIZE_ALIAS, size)
  }

  fun getMarginSize() = sharedPrefs.getInt(MARGIN_SIZE_ALIAS, DEFAULT_MARGIN_SIZE).dp

  fun storeOrder(isReversed: Boolean) {
    saveBoolean(IS_REVERSED_ORDER_ALIAS, isReversed)
  }

  fun isOrderReversed() = sharedPrefs.getBoolean(IS_REVERSED_ORDER_ALIAS, false)

  fun hasRootAccess() =
    if (sharedPrefs.contains(ROOT_ACCESS_GRANTED_ALIAS)) {
      sharedPrefs.getBoolean(ROOT_ACCESS_GRANTED_ALIAS, false)
    } else {
      refreshRootAccess()
    }

  fun refreshRootAccess(forcedValue: Boolean? = null) =
    (forcedValue ?: rootManager.checkRootAccess()).let {
      saveBoolean(ROOT_ACCESS_GRANTED_ALIAS, it)
      it
    }

  private fun saveBoolean(key: String, value: Boolean) {
    sharedPrefs.edit(commit = true) {
      putBoolean(key, value)
    }
  }

  private fun saveInt(key: String, value: Int) {
    sharedPrefs.edit(commit = true) {
      putInt(key, value)
    }
  }
}
