package com.tymwitko.recents.kill

import android.util.Log
import com.tymwitko.recents.common.accessors.RootManager
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.settings.whitelist.db.WhitelistRepository

class AppKiller(
  private val whitelistRepository: WhitelistRepository,
  private val shizukuManager: ShizukuManager,
  private val rootManager: RootManager
) {
  suspend fun killApp(app: App) {
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
