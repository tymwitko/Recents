package com.tymwitko.recents.settings.whitelist.db

import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.common.db.RecentsDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WhitelistRepository(private val recentsDao: RecentsDao) {

  suspend fun getEntry(packageId: String): PackageSettings? =
    withContext(Dispatchers.IO) {
      recentsDao.getFromWhitelistByPackageId(packageId)?.toDomain()
    }

  suspend fun canKill(packageId: String) = withContext(Dispatchers.IO) {
    recentsDao.getFromWhitelistByPackageId(packageId)?.canKill ?: true
  }

  suspend fun setKilling(app: App, canKill: Boolean) {
    withContext(Dispatchers.IO) {
      with(recentsDao) {
        val oldEntry = getFromWhitelistByPackageId(app.getId())
        oldEntry?.let {
          updateWhitelist(
            WhitelistEntry(
              packageName = app.packageName,
              isWorkApp = app.isWorkApp,
              canKill = canKill,
              canLaunch = oldEntry.canLaunch,
              canShow = oldEntry.canShow
            )
          )
        } ?: run {
          insertToWhitelist(
            WhitelistEntry(
              packageName = app.packageName,
              isWorkApp = app.isWorkApp,
              canKill = canKill
            )
          )
        }
      }
    }
  }

  suspend fun canLaunch(packageId: String) = withContext(Dispatchers.IO) {
    recentsDao.getFromWhitelistByPackageId(packageId)?.canLaunch ?: true
  }

  suspend fun setLaunching(app: App, canLaunch: Boolean) {
    withContext(Dispatchers.IO) {
      with(recentsDao) {
        val oldEntry = getFromWhitelistByPackageId(app.getId())
        oldEntry?.let {
          updateWhitelist(
            WhitelistEntry(
              packageName = app.packageName,
              isWorkApp = app.isWorkApp,
              canKill = oldEntry.canKill,
              canLaunch = canLaunch,
              canShow = oldEntry.canShow
            )
          )
        } ?: run {
          insertToWhitelist(
            WhitelistEntry(
              packageName = app.packageName,
              isWorkApp = app.isWorkApp,
              canLaunch = canLaunch
            )
          )
        }
      }
    }
  }

  suspend fun canShow(packageId: String) = withContext(Dispatchers.IO) {
    recentsDao.getFromWhitelistByPackageId(packageId)?.canShow ?: true
  }

  suspend fun setShowing(app: App, canShow: Boolean) {
    withContext(Dispatchers.IO) {
      with(recentsDao) {
        val oldEntry = getFromWhitelistByPackageId(app.getId())
        oldEntry?.let {
          updateWhitelist(
            WhitelistEntry(
              packageName = app.packageName,
              isWorkApp = app.isWorkApp,
              canKill = oldEntry.canKill,
              canLaunch = oldEntry.canLaunch,
              canShow = canShow
            )
          )
        } ?: run {
          insertToWhitelist(
            WhitelistEntry(
              packageName = app.packageName,
              isWorkApp = app.isWorkApp,
              canShow = canShow
            )
          )
        }
      }
    }
  }
}
