package com.tymwitko.recents.common.accessors

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.tymwitko.recents.common.FREEFORM_MODE
import com.tymwitko.recents.common.LAUNCH_WINDOWING_EXTRA
import com.tymwitko.recents.common.LAUNCH_WINDOWING_METHOD_NAME
import com.tymwitko.recents.common.SPLIT_MODE_SECONDARY
import com.tymwitko.recents.common.WINDOWING_EXTRA
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.common.dataclasses.DumpApp
import org.lsposed.hiddenapibypass.HiddenApiBypass

class IntentSender(context: Context) {
  private val packageManager = context.packageManager
  private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
  private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

  @SuppressLint("NewApi")
  fun launchSelectedApp(
    app: App,
    startActivity: (Intent, Bundle?) -> Unit,
    customIntent: Intent? = null,
    isFreeForm: Boolean = false
  ): Boolean =
    (app as? DumpApp)
      ?.takeIf { Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && it.isWorkApp }
      ?.let {
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

  fun launchFreeForm(app: App, startActivity: (Intent, Bundle?) -> Unit) {
    val freeFormIntent = packageManager.getLaunchIntentForPackage(app.packageName) ?: Intent()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      runCatching { forceViaLsposed(true) }
    }
    freeFormIntent.addFlags(
      Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
    )
    freeFormIntent.putExtra(WINDOWING_EXTRA, FREEFORM_MODE)
    freeFormIntent.putExtra(LAUNCH_WINDOWING_EXTRA, FREEFORM_MODE)
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
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      runCatching { forceViaLsposed(false) }
    }
    splitIntent.addFlags(
      Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or
        Intent.FLAG_ACTIVITY_NEW_TASK or
        Intent.FLAG_ACTIVITY_MULTIPLE_TASK
    )
    splitIntent.putExtra(WINDOWING_EXTRA, SPLIT_MODE_SECONDARY)
    splitIntent.putExtra(LAUNCH_WINDOWING_EXTRA, SPLIT_MODE_SECONDARY)
    launchSelectedApp(app, startActivity, splitIntent)
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

  private fun getFreeFormBundle(): Bundle? {
    with(getScreenDimensions()) {
      val start = first?.div(10)
      val top = second?.div(10)
      val end = first?.times(0.9)?.toInt()
      val bottom = second?.times(0.9)?.toInt()

      val options = ActivityOptions.makeBasic()
      options.launchBounds = runCatching {
        Rect(start!!, top!!, end!!, bottom!!)
      }.getOrNull()

      val method = runCatching {
        ActivityOptions::class.java.getMethod(
          LAUNCH_WINDOWING_METHOD_NAME,
          Int::class.javaPrimitiveType
        )
      }.getOrNull()
      method?.invoke(options, FREEFORM_MODE)
      return options.toBundle()
    }
  }

  @RequiresApi(Build.VERSION_CODES.P)
  private fun forceViaLsposed(isFreeform: Boolean) {
    HiddenApiBypass.invoke(
      ActivityOptions::class.java,
      ActivityOptions.makeBasic(),
      LAUNCH_WINDOWING_METHOD_NAME,
      if (isFreeform) FREEFORM_MODE else SPLIT_MODE_SECONDARY
    )
  }
}
