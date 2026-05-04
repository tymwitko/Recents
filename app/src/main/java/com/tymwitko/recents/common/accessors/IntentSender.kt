package com.tymwitko.recents.common.accessors

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.common.dataclasses.DumpApp
import com.tymwitko.recents.common.exceptions.AppNotLaunchedException
import org.koin.core.component.KoinComponent

class IntentSender(private val packageManager: PackageManager, private val launcherApps: LauncherApps): KoinComponent {
  @SuppressLint("NewApi")
  fun launchSelectedApp(app: App, startActivity: (Intent) -> Unit): Boolean =
    (app as? DumpApp)?.takeIf { Build.VERSION.SDK_INT >= Build.VERSION_CODES.O }?.let {
      try {
        launcherApps.startMainActivity(
          app.componentName,
          launcherApps.profiles.last(),
          Rect(),
          Bundle.EMPTY
        )
        true
      } catch (_: Exception) {
        launchForDefaultUser(app.packageName, startActivity)
      }
    } ?: run {
      launchForDefaultUser(app.packageName, startActivity)
    }

  fun launchLastApp(appList: List<App>, startActivity: (Intent) -> Unit) {
    appList.forEach {
      if (launchSelectedApp(it, startActivity)) return
    }
    Log.d(
      "TAG",
      "FAILED TO LAUNCH ANYTHING"
    )
    throw AppNotLaunchedException()
  }
  
  private fun launchForDefaultUser(packageName: String, startActivity: (Intent) -> Unit) =
    packageManager
      .getLaunchIntentForPackage(packageName)
      ?.let {
        startActivity(it)
        true
      } ?: false
}
