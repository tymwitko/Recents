package com.tymwitko.recents.common.accessors

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log
import android.view.accessibility.AccessibilityManager
import com.tymwitko.recents.common.exceptions.AppNotKilledException
import com.tymwitko.recents.whitelist.db.WhitelistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataOutputStream

class AppKiller(
  private val packageManager: PackageManager,
  private val accessibilityManager: AccessibilityManager,
  private val whitelistRepository: WhitelistRepository
) {
  suspend fun killByPackageInfo(packageInfo: PackageInfo) {
    withContext(Dispatchers.IO) {
      if ((ApplicationInfo.FLAG_SYSTEM and (packageInfo.applicationInfo?.flags ?: 0)) == 0 &&
        canKill(packageInfo.packageName)
      ) {
        try {
          Log.d("TAG", "Gonna close ${packageInfo.packageName}")
          val suProcess = Runtime.getRuntime().exec("su")
          val os = DataOutputStream(suProcess.outputStream)
          os.writeBytes("am force-stop ${packageInfo.packageName}\n")
          os.flush()
        } catch (e: Exception) {
          e.printStackTrace()
          Log.d("TAG", "Caught!!!")
          throw AppNotKilledException()
        }
      } else {
        Log.d("TAG", "${packageInfo.packageName} is whitelisted or a system app!")
        throw AppNotKilledException()
      }
    }
  }

  fun hasAccessibilityService(packageName: String): Boolean {
    val runningServices =
      accessibilityManager.getEnabledAccessibilityServiceList(
        AccessibilityServiceInfo.FEEDBACK_ALL_MASK
      )
    for (service in runningServices) {
      if (service.resolveInfo.serviceInfo.packageName == packageName) {
        return true
      }
    }
    Log.d("TAG", "$packageName has an accessibility service")
    return false
  }

  fun hasSetAlarmPermission(packageName: String?) =
    packageManager.checkPermission(
      Manifest.permission.SET_ALARM,
      packageName!!
    ) == PackageManager.PERMISSION_GRANTED

  private suspend fun canKill(packageName: String) = whitelistRepository.canKill(packageName)
}
