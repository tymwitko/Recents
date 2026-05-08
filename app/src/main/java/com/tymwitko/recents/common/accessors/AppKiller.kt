package com.tymwitko.recents.common.accessors

import android.util.Log
import com.scottyab.rootbeer.RootBeer
import com.tymwitko.recents.common.exceptions.AppNotKilledException
import com.tymwitko.recents.settings.whitelist.db.WhitelistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataOutputStream

class AppKiller(
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

  private suspend fun canKill(packageName: String) = whitelistRepository.canKill(packageName)
}
