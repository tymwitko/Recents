package com.tymwitko.recents.common.accessors

import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import com.tymwitko.recents.common.exceptions.AppNotLaunchedException
import org.koin.core.component.KoinComponent

class IntentSender(private val packageManager: PackageManager): KoinComponent {
  fun launchSelectedApp(packageName: String, startActivity: (Intent) -> Unit): Boolean {
    packageManager
      .getLaunchIntentForPackage(packageName)
      ?.let {
        startActivity(it)
        return true
      }
    Log.d("TAG", "Launching app failed, possibly it lacks an Activity")
    return false
  }

  fun launchLastApp(appList: List<String>, startActivity: (Intent) -> Unit) {
    appList.forEach {
      if (launchSelectedApp(it, startActivity)) return
    }
    Log.d(
      "TAG",
      "FAILED TO LAUNCH ANYTHING"
    )
    throw AppNotLaunchedException()
  }
}
