package com.tymwitko.recents.common.accessors

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

class ShizukuManager {
  
  private lateinit var resultListener: Shizuku.OnRequestPermissionResultListener
  private var denied = false
  
  fun isShizukuAllowed() = 
    try {
      Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    } catch (_: IllegalStateException) {
      false
    }
  
  fun setupPermissionListener(thisPackageName: String, onResult: (Int, Int) -> Unit) {
    resultListener =
      Shizuku.OnRequestPermissionResultListener { code, result ->
        denied = result == -1
        if (!denied) requestViaShizuku(thisPackageName)
        onResult(code, result)
      }
    Shizuku.addRequestPermissionResultListener(resultListener)
  }
  
  fun shutdownShizuku() {
    Shizuku.removeRequestPermissionResultListener(resultListener)
  }
  
  private fun checkPermission(code: Int) = when {
    Shizuku.isPreV11() || Shizuku.shouldShowRequestPermissionRationale() -> false
    isShizukuAllowed() -> true
    else -> {
      Shizuku.requestPermission(code)
      false
    }
  }
  
  fun requestShizukuPermission() {
    if (denied) return
    if (!checkPermission(SHIZUKU_PERMISSION_REQUEST_CODE)) {
      Shizuku.requestPermission(SHIZUKU_PERMISSION_REQUEST_CODE)
      return
    }
  }
  
  fun killWithShizuku(packageName: String) {
    executeCommand("am force-stop $packageName")
  }

  private fun requestViaShizuku(thisPackageName: String) {
    executeCommand("pm grant $thisPackageName ${Manifest.permission.PACKAGE_USAGE_STATS}")
  }
  
  private fun executeCommand(command: String) {
    try {
      val newProcessMethod = Shizuku::class.java.getDeclaredMethod(
        "newProcess",
        Array<String>::class.java,
        Array<String>::class.java,
        String::class.java
      ).apply {
        isAccessible = true
      }

      val process = newProcessMethod.invoke(null, arrayOf("sh", "-c", command), null, null) as Process

      val output = mutableListOf<String>()
      val error = mutableListOf<String>()

      BufferedReader(
        InputStreamReader(process.inputStream))
        .use { reader ->
          reader.lineSequence().forEach { line ->
            output.add(line)
          }
        }
      BufferedReader(
        InputStreamReader(process.errorStream)
      ).use { reader ->
        reader.lineSequence().forEach { line ->
          error.add(line)
        }
      }

    } catch (e: InterruptedException) {
      Log.w("TAG", e.stackTrace.toString())
      e.printStackTrace()
    }
  }
  
  companion object {
    const val SHIZUKU_PERMISSION_REQUEST_CODE = 2137
  }
}