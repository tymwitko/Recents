package com.tymwitko.recents.accessors

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log
import android.view.accessibility.AccessibilityManager
import com.tymwitko.recents.consts.Whitelist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataOutputStream

class AppKiller {
    suspend fun killByPackageInfo(packageInfo: PackageInfo): Boolean {
        if ((ApplicationInfo.FLAG_SYSTEM and (packageInfo.applicationInfo?.flags ?: 0)) == 0 &&
            !Whitelist.isWhitelistedAgainstKilling(packageInfo.packageName)) {
            try {
                Log.d("TAG", "Gonna close ${packageInfo.packageName}")
                withContext(Dispatchers.IO) {
                    val suProcess = Runtime.getRuntime().exec("su")
                    val os = DataOutputStream(suProcess.outputStream)
                    os.writeBytes("am force-stop ${packageInfo.packageName}\n")
                    os.flush()
                }
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        } else {
            Log.d("TAG", "${packageInfo.packageName} is whitelisted or a system app!")
            return false
        }
    }

    fun hasAccessibilityService(packageName: String, context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val runningServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        for (service in runningServices) {
            if (service.resolveInfo.serviceInfo.packageName == packageName) {
                return true
            }
        }
        Log.d("TAG", "$packageName has an accessibility service")
        return false
    }

    fun hasSetAlarmPermission(context: Context, packageName: String?) =
        context.packageManager.checkPermission(
            Manifest.permission.SET_ALARM,
            packageName!!
        ) == PackageManager.PERMISSION_GRANTED
}