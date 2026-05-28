package com.tymwitko.recents.settings.whitelist.db

import com.tymwitko.recents.common.db.RecentsDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WhitelistRepository(private val recentsDao: RecentsDao) {

  suspend fun getEntry(packageName: String): PackageSettings? =
    withContext(Dispatchers.IO) {
      recentsDao.getFromWhitelistByPackageName(packageName)?.toDomain()
    }

  suspend fun canKill(packageName: String) = withContext(Dispatchers.IO) {
    recentsDao.getFromWhitelistByPackageName(packageName)?.canKill ?: true
  }

  suspend fun setKilling(packageName: String, canKill: Boolean) {
    withContext(Dispatchers.IO) {
      with(recentsDao) {
        val oldEntry = getFromWhitelistByPackageName(packageName)
        oldEntry?.let {
          updateWhitelist(
            WhitelistEntry(
              packageName = packageName,
              canKill = canKill,
              canLaunch = oldEntry.canLaunch,
              canShow = oldEntry.canShow
            )
          )
        } ?: run {
          insertToWhitelist(
            WhitelistEntry(packageName = packageName, canKill = canKill)
          )
        }
      }
    }
  }

  suspend fun canLaunch(packageName: String) = withContext(Dispatchers.IO) {
    recentsDao.getFromWhitelistByPackageName(packageName)?.canLaunch ?: true
  }

  suspend fun setLaunching(packageName: String, canLaunch: Boolean) {
    withContext(Dispatchers.IO) {
      with(recentsDao) {
        val oldEntry = getFromWhitelistByPackageName(packageName)
        oldEntry?.let {
          updateWhitelist(
            WhitelistEntry(
              packageName = packageName,
              canKill = oldEntry.canKill,
              canLaunch = canLaunch,
              canShow = oldEntry.canShow
            )
          )
        } ?: run {
          insertToWhitelist(
            WhitelistEntry(packageName = packageName, canLaunch = canLaunch)
          )
        }
      }
    }
  }

  suspend fun canShow(packageName: String) = withContext(Dispatchers.IO) {
    recentsDao.getFromWhitelistByPackageName(packageName)?.canShow ?: true
  }

  suspend fun setShowing(packageName: String, canShow: Boolean) {
    withContext(Dispatchers.IO) {
      with(recentsDao) {
        val oldEntry = getFromWhitelistByPackageName(packageName)
        oldEntry?.let {
          updateWhitelist(
            WhitelistEntry(
              packageName = packageName,
              canKill = oldEntry.canKill,
              canLaunch = oldEntry.canLaunch,
              canShow = canShow
            )
          )
        } ?: run {
          insertToWhitelist(
            WhitelistEntry(packageName = packageName, canShow = canShow)
          )
        }
      }
    }
  }
  
  suspend fun setDefaultWhitelistSettings(packageName: String, canShow: Boolean, canKill: Boolean) {
    withContext(Dispatchers.IO) {
      with(recentsDao) {
        if (getFromWhitelistByPackageName(packageName) == null)
          insertToWhitelist(WhitelistEntry(packageName = packageName, canShow = canShow, canKill = canKill))
      }
    }
  }
}