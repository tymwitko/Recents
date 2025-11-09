package com.tymwitko.recents.accessors

import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import com.tymwitko.recents.exceptions.AppNotLaunchedException
import org.koin.core.component.KoinComponent

class IntentSender(private val packageManager: PackageManager): KoinComponent {

    // fun launchPermissionSettings(context: Context) {
    //     startActivity(context, Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), null)
    // }

    fun launchSelectedApp(packageName: String, startActivity: (Intent) -> Unit): Boolean {
        packageManager
            .getLaunchIntentForPackage(packageName)
            ?.let {
                Log.d("TAG","Launching app $it")
                startActivity(it)
                return true
            }
        Log.d("TAG", "Launching app failed, possibly it lacks an Activity")
        return false
    }

    fun launchLastApp(appList: List<String>, startActivity: (Intent) -> Unit) {
        appList.forEach {
            Log.d("TAG", "last app is $it")
            if (launchSelectedApp(it, startActivity)) return
        }
        Log.d(
            "TAG",
            "FAILED TO LAUNCH ANYTHING"
        )
        throw AppNotLaunchedException()
    }
}