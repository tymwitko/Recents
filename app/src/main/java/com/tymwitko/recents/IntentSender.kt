package com.tymwitko.recents

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity

class IntentSender {

    fun launchPermissionSettings(context: Context) {
        startActivity(context, Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), null)
    }

    fun launchSelectedApp(context: Context, packageName: String) {
        context
            .packageManager
            .getLaunchIntentForPackage(packageName)
            ?.let {
                startActivity(context, it, null)
            }
    }

    fun launchLastApp(context: Context, appList: List<String>) {
        appList.forEach {
                Log.d("TAG", "last app is $it")
                val launchIntent = context.packageManager?.getLaunchIntentForPackage(it)
                launchIntent?.let {
                    Log.d("TAG","Launching app $it")
                    context.startActivity(launchIntent)
                    return
                }
                Log.d("TAG", "Launching app failed, possibly it lacks an Activity")
            }
        Toast.makeText(
            context,
            R.string.failed_to_launch_anything,
            Toast.LENGTH_LONG
        ).show()
    }
}