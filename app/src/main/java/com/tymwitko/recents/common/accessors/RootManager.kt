package com.tymwitko.recents.common.accessors

import android.Manifest
import android.os.Build
import com.scottyab.rootbeer.RootBeer
import com.tymwitko.recents.BuildConfig
import java.io.DataOutputStream

class RootManager(
  private val rootBeer: RootBeer
) {
  fun killWithRoot(packageName: String) = executeCommand("am force-stop $packageName\n")

  fun hasRoot() = true
//    rootBeer.isRooted

  fun getPermissions(): Boolean =
    runCatching {
      executeCommand("pm grant ${BuildConfig.APPLICATION_ID} ${Manifest.permission.PACKAGE_USAGE_STATS}")
      executeCommand("pm grant ${BuildConfig.APPLICATION_ID} ${Manifest.permission.DUMP}")
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
        executeCommand("pm grant ${BuildConfig.APPLICATION_ID} ${Manifest.permission.ACCESS_HIDDEN_PROFILES}")
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        executeCommand(
          "pm grant ${BuildConfig.APPLICATION_ID} android.permission.INTERACT_ACROSS_USERS_FULL"
        )
      hasRoot()
    }.getOrDefault(false)

  private fun executeCommand(command: String) {
    val suProcess = Runtime.getRuntime().exec("su")
    val os = DataOutputStream(suProcess.outputStream)
    os.writeBytes(command)
    os.flush()
  }
}
