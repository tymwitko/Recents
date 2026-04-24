package com.tymwitko.recents.settings.ui

import android.content.SharedPreferences
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.tymwitko.recents.common.DEFAULT_FONT_SIZE
import com.tymwitko.recents.common.SIZE_ALIAS

class UiSettingsHolder(private val sharedPrefs: SharedPreferences) {
  fun storeFontSize(newSize: Int) {
    sharedPrefs.edit(commit = true) {
      putInt(SIZE_ALIAS, newSize)
    }
  }

  fun getFontSize() = sharedPrefs.getInt(SIZE_ALIAS, DEFAULT_FONT_SIZE).sp
}