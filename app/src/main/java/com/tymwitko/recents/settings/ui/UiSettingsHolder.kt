package com.tymwitko.recents.settings.ui

import android.content.SharedPreferences
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.tymwitko.recents.common.DEFAULT_FONT_SIZE
import com.tymwitko.recents.common.FONT_SIZE_ALIAS
import com.tymwitko.recents.common.ICON_SIZE_ALIAS

class UiSettingsHolder(private val sharedPrefs: SharedPreferences) {
  fun storeFontSize(newSize: Int) {
    sharedPrefs.edit(commit = true) {
      putInt(FONT_SIZE_ALIAS, newSize)
    }
  }

  fun getFontSize() = sharedPrefs.getInt(FONT_SIZE_ALIAS, DEFAULT_FONT_SIZE).sp

  fun storeIconSize(newSize: Int) {
    sharedPrefs.edit(commit = true) {
      putInt(ICON_SIZE_ALIAS, newSize)
    }
  }

  fun getIconSize(defaultSize: Int)   = sharedPrefs.getInt(ICON_SIZE_ALIAS, defaultSize).dp
}