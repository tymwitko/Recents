package com.tymwitko.recents.common.accessors

import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import org.koin.core.component.KoinComponent

class IconAccessor(private val packageManager: PackageManager): KoinComponent {
  fun getAppIcon(packageName: String): ImageBitmap? =
    try {
      packageManager.getApplicationIcon(packageName).toBitmap().asImageBitmap()
    } catch (e: PackageManager.NameNotFoundException) {
      Log.w("TAG", e.stackTrace.toString())
      null
    }
}
