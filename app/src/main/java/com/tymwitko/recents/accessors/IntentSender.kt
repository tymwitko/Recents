package com.tymwitko.recents.accessors

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.tymwitko.recents.R
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class IntentSender: KoinComponent {

    private val recentAppsAccessor: RecentAppsAccessor by inject()

    // fun launchPermissionSettings(context: Context) {
    //     startActivity(context, Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), null)
    // }

    fun launchSelectedApp(context: Context, packageName: String): Boolean {
        context
            .packageManager
            .getLaunchIntentForPackage(packageName)
            ?.let {
                Log.d("TAG","Launching app $it")
                startActivity(context, it, null)
                return true
            }
        Log.d("TAG", "Launching app failed, possibly it lacks an Activity")
        return false
    }

    fun launchLastApp(context: Context, appList: List<String>) {
        appList.forEach {
            Log.d("TAG", "last app is $it")
            if (launchSelectedApp(context, it)) return
        }
        Toast.makeText(
            context,
            R.string.failed_to_launch_anything,
            Toast.LENGTH_LONG
        ).show()
    }
}