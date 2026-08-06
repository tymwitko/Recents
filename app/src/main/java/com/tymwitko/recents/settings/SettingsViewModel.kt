package com.tymwitko.recents.settings

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.tymwitko.recents.common.EXTRACT_LOGCAT_COMMAND

class SettingsViewModel(private val settingsHolder: SettingsHolder) : ViewModel() {
  fun saveLogsToUri(contentResolver: ContentResolver, uri: Uri) {
    try {
      contentResolver.openOutputStream(uri)?.use { outStream ->
        val process = Runtime.getRuntime().exec(EXTRACT_LOGCAT_COMMAND)
        process.inputStream.use { input ->
          input.copyTo(outStream)
        }
        process.waitFor()
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun getFontSize() = settingsHolder.getFontSize()
}