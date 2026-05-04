package com.tymwitko.recents.common.accessors

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.pm.PackageManager
import android.util.Log
import android.view.accessibility.AccessibilityManager
import com.scottyab.rootbeer.RootBeer
import com.tymwitko.recents.common.exceptions.AppNotKilledException
import com.tymwitko.recents.settings.whitelist.db.WhitelistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataOutputStream

class AppKiller(
  private val packageManager: PackageManager,
  private val accessibilityManager: AccessibilityManager,
  private val whitelistRepository: WhitelistRepository,
  private val rootBeer: RootBeer,
  private val shizukuManager: ShizukuManager
) {
  suspend fun killByPackageName(packageName: String) {
    withContext(Dispatchers.IO) {
      if (canKill(packageName)) {
        try {
          if (rootBeer.isRooted) killWithRoot(packageName)
          else shizukuManager.killWithShizuku(packageName)
        } catch (e: Exception) {
          Log.w("TAG", "app not killed, cause ${e.stackTrace}")
          throw AppNotKilledException()
        }
      } else {
        throw AppNotKilledException()
      }
    }
  }

  fun killWithRoot(packageName: String) {
    val suProcess = Runtime.getRuntime().exec("su")
    val os = DataOutputStream(suProcess.outputStream)
    os.writeBytes("am force-stop $packageName\n")
    os.flush()
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
