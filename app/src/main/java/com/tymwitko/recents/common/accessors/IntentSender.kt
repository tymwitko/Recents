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

class IntentSender(
  private val packageManager: PackageManager,
  private val launcherApps: LauncherApps
) : KoinComponent {
  @SuppressLint("NewApi")
  fun launchSelectedApp(
    app: App,
    startActivity: (Intent) -> Unit,
    customIntent: Intent? = null
  ): Boolean =
    (app as? DumpApp)?.takeIf { Build.VERSION.SDK_INT >= Build.VERSION_CODES.O }?.let {
      try {
        val optionsBundle = customIntent?.let {
          Bundle().apply {
            putParcelable(Intent.EXTRA_INTENT, customIntent)
          }
        } ?: Bundle.EMPTY
        launcherApps.startMainActivity(
          app.componentName,
          launcherApps.profiles.last(),
          Rect(),
          optionsBundle
        )
        true
      } catch (_: Exception) {
        launchForDefaultUser(app.packageName, startActivity, customIntent)
      }
    } ?: run {
      launchForDefaultUser(app.packageName, startActivity, customIntent)
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

  private fun launchForDefaultUser(
    packageName: String,
    startActivity: (Intent) -> Unit,
    customIntent: Intent?
  ) =
    (customIntent ?: packageManager
      .getLaunchIntentForPackage(packageName))
      ?.let {
        startActivity(it)
        true
      } ?: false

  fun goToSplitMode(app: App, startActivity: (Intent) -> Unit) {
    val manager = packageManager
    val i = manager.getLaunchIntentForPackage(app.packageName) ?: Intent()
    i.addFlags(
      Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or
        Intent.FLAG_ACTIVITY_NEW_TASK or
        Intent.FLAG_ACTIVITY_MULTIPLE_TASK
    )
    launchSelectedApp(app, startActivity, i)
  }
}
