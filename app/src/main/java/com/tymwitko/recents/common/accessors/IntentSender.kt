package com.tymwitko.recents.common.accessors

import android.annotation.SuppressLint
import android.app.ActivityOptions
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
    startActivity: (Intent, Bundle?) -> Unit,
    customIntent: Intent? = null,
    isFreeForm: Boolean = false
  ): Boolean =
    (app as? DumpApp)?.takeIf { Build.VERSION.SDK_INT >= Build.VERSION_CODES.O }?.let {
      try {
        val optionsBundle = when {
          customIntent != null -> Bundle().apply { 
            putParcelable(Intent.EXTRA_INTENT, customIntent)
          }
          isFreeForm -> getFreeFormBundle()
          else -> Bundle.EMPTY
        }
        launcherApps.startMainActivity(
          app.componentName,
          launcherApps.profiles.last(),
          Rect(100, 100, 200, 200),
          optionsBundle
        )
        true
      } catch (_: Exception) {
        launchForDefaultUser(app.packageName, startActivity, customIntent)
      }
    } ?: run {
      launchForDefaultUser(app.packageName, startActivity, customIntent)
    }

  fun launchLastApp(appList: List<App>, startActivity: (Intent, Bundle?) -> Unit) {
    appList.forEach {
      if (launchSelectedApp(it, startActivity)) return
    }
    Log.d(
      "TAG",
      "FAILED TO LAUNCH ANYTHING"
    )
    throw AppNotLaunchedException()
  }

  fun launchFreeForm(app: App, startActivity: (Intent, Bundle?) -> Unit) {
    val freeFormIntent = packageManager.getLaunchIntentForPackage(app.packageName) ?: Intent()
    freeFormIntent.addFlags(
      Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
    )
    launchSelectedApp(app, startActivity, freeFormIntent)
  }

  private fun launchForDefaultUser(
    packageName: String,
    startActivity: (Intent, Bundle?) -> Unit,
    customIntent: Intent?
  ) =
    (customIntent ?: packageManager
      .getLaunchIntentForPackage(packageName))
      ?.let {
        startActivity(it, getFreeFormBundle())
        true
      } ?: false

  fun goToSplitMode(app: App, startActivity: (Intent, Bundle?) -> Unit) {
    val splitIntent = packageManager.getLaunchIntentForPackage(app.packageName) ?: Intent()
    splitIntent.addFlags(
      Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or
        Intent.FLAG_ACTIVITY_NEW_TASK or
        Intent.FLAG_ACTIVITY_MULTIPLE_TASK
    )
    launchSelectedApp(app, startActivity, splitIntent)
  }
  
  private fun getFreeFormBundle(): Bundle? {
    val start = 144
    val top = 144
    val end = 433
    val bottom = 433
    
    return getFreeFormOptions().apply {
      launchBounds = Rect(start, top, end, bottom)
    }.toBundle()
  }

  fun getFreeFormOptions(): ActivityOptions {
    val options = ActivityOptions.makeBasic()
    val method = ActivityOptions::class.java.getMethod(
      "setLaunchWindowingMode",
      Int::class.javaPrimitiveType
    )
    method.invoke(options, 5)
    return options
  }
}
