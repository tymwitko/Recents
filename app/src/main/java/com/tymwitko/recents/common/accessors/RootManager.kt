package com.tymwitko.recents.common.accessors

import android.Manifest
import android.os.Build
import com.tymwitko.recents.BuildConfig
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.lang.System.`in`


class RootManager {
  fun killWithRoot(packageName: String) = executeCommand("am force-stop $packageName\n")

  fun checkRootAccess() =
    runCatching {
      val suProcess = Runtime.getRuntime().exec("su")
      val os = DataOutputStream(suProcess.outputStream)
      val osRes = BufferedReader(InputStreamReader(`in`))
      os.writeBytes("id\n")
      os.flush()
      val currUid: String? = osRes.readLine()
      currUid?.contains("uid=0")
        ?.also {
          os.writeBytes("exit\n")
          os.flush()
        } ?: false
    }.getOrDefault(false) // todo

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
      true
    }.getOrDefault(false)

  private fun executeCommand(command: String) {
    val suProcess = Runtime.getRuntime().exec("su")
    val os = DataOutputStream(suProcess.outputStream)
    os.writeBytes(command)
    os.writeBytes("exit\n")
    os.flush()
  }
}
