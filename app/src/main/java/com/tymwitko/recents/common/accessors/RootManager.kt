package com.tymwitko.recents.common.accessors

import android.Manifest
import android.os.Build
import com.scottyab.rootbeer.RootBeer
import java.io.DataOutputStream

class RootManager(
  private val rootBeer: RootBeer
) {
  fun killWithRoot(packageName: String) = executeCommand("am force-stop $packageName\n")

  fun hasRoot() =
    rootBeer.isRooted

  fun getPermissions(thisPackageName: String): Boolean {
    executeCommand("pm grant $thisPackageName ${Manifest.permission.PACKAGE_USAGE_STATS}")
    executeCommand("pm grant $thisPackageName ${Manifest.permission.DUMP}")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
      executeCommand("pm grant $thisPackageName ${Manifest.permission.ACCESS_HIDDEN_PROFILES}")
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
      executeCommand(
        "pm grant $thisPackageName android.permission.INTERACT_ACROSS_USERS_FULL"
      )
    return hasRoot()
  }

  private fun executeCommand(command: String) {
    val suProcess = Runtime.getRuntime().exec("su")
    val os = DataOutputStream(suProcess.outputStream)
    os.writeBytes(command)
    os.flush()
  }
}
