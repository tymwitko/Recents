package com.tymwitko.recents.common.accessors

import android.util.Log
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.common.exceptions.AppNotKilledException
import com.tymwitko.recents.settings.whitelist.db.WhitelistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppKiller(
  private val whitelistRepository: WhitelistRepository,
  private val shizukuManager: ShizukuManager,
  private val rootManager: RootManager
) {
  suspend fun killApp(app: App) {
    withContext(Dispatchers.IO) {
      if (whitelistRepository.canKill(app.getId())) {
        try {
          if (shizukuManager.isShizukuAllowed()) shizukuManager.killWithShizuku(app.packageName)
          else rootManager.killWithRoot(app.packageName)
        } catch (e: Exception) {
          Log.w("TAG", "app not killed, cause ${e.stackTrace}")
          throw AppNotKilledException()
        }
      } else {
        throw AppNotKilledException()
      }
    }
  }
}
