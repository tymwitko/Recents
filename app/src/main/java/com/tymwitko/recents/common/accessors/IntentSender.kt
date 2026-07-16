package com.tymwitko.recents.common.accessors

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowInsets
import android.view.WindowManager
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.common.dataclasses.DumpApp
import com.tymwitko.recents.common.exceptions.AppNotLaunchedException
import org.koin.core.component.KoinComponent

class IntentSender(
  private val packageManager: PackageManager,
  private val launcherApps: LauncherApps,
  private val windowManager: WindowManager
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
        launchForDefaultUser(app.packageName, startActivity, customIntent, isFreeForm)
      }
    } ?: run {
      launchForDefaultUser(app.packageName, startActivity, customIntent, isFreeForm)
    }

  fun launchLastApp(appList: List<App>, startActivity: (Intent, Bundle?) -> Unit) {
    appList.forEach {
      if (launchSelectedApp(it, startActivity)) return
    }
    throw AppNotLaunchedException()
  }

  fun launchFreeForm(app: App, startActivity: (Intent, Bundle?) -> Unit) {
    val freeFormIntent = packageManager.getLaunchIntentForPackage(app.packageName) ?: Intent()
    freeFormIntent.addFlags(
      Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
    )
    launchSelectedApp(app, startActivity, freeFormIntent, isFreeForm = true)
  }

  fun launchUsageAccessSettings(startActivity: (Intent) -> Unit) {
    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    startActivity(intent)
  }

  private fun launchForDefaultUser(
    packageName: String,
    startActivity: (Intent, Bundle?) -> Unit,
    customIntent: Intent?,
    isFreeForm: Boolean
  ) =
    (customIntent ?: packageManager
      .getLaunchIntentForPackage(packageName))
      ?.let {
        startActivity(
          it,
          if (isFreeForm) getFreeFormBundle() else null
        )
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
    with(getScreenDimensions()) {
      val start = first?.div(10)
      val top = second?.div(10)
      val end = first?.times(0.9)?.toInt()
      val bottom = second?.times(0.9)?.toInt()

      return if (null in listOf(start, top, end, bottom)) null
      else getFreeFormOptions().apply {
        launchBounds = Rect(start!!, top!!, end!!, bottom!!)
      }.toBundle()
    }
  }

  @Suppress("DEPRECATION")
  private fun getScreenDimensions(): Pair<Int?, Int?> {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      val windowMetrics = windowManager.currentWindowMetrics
      with(
        windowMetrics.windowInsets.getInsetsIgnoringVisibility(
          WindowInsets.Type.systemBars()
        )
      ) {
        val width = windowMetrics.bounds.width() - left - right
        val height = windowMetrics.bounds.height() - top - bottom
        return Pair(width, height)
      }
    } else {
      val displayMetrics = DisplayMetrics()
      windowManager.defaultDisplay.getMetrics(displayMetrics)
      return Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }
  }

  private fun getFreeFormOptions(): ActivityOptions {
    val options = ActivityOptions.makeBasic()
    val method = ActivityOptions::class.java.getMethod(
      "setLaunchWindowingMode",
      Int::class.javaPrimitiveType
    )
    method.invoke(options, 5)
    return options
  }
}
