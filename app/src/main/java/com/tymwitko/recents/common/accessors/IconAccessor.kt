package com.tymwitko.recents.common.accessors

import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import org.koin.core.component.KoinComponent

class IconAccessor(context: Context): KoinComponent {
  private val packageManager = context.packageManager

  fun getAppIcon(packageName: String): ImageBitmap? =
    try {
      packageManager.getApplicationIcon(packageName).toBitmap().asImageBitmap()
    } catch (e: PackageManager.NameNotFoundException) {
      Log.w("TAG", e.stackTraceToString())
      null
    }

  fun getAppIconForWorkApp(launcherActivityInfo: LauncherActivityInfo) =
    launcherActivityInfo.getBadgedIcon(0).toBitmap().asImageBitmap()
}
