package com.tymwitko.recents.settings.whitelist.db

import com.tymwitko.recents.whitelist.db.PackageSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WhitelistRepository(private val whitelistDao: WhitelistDao) {

  suspend fun getEntry(packageName: String): PackageSettings? =
    withContext(Dispatchers.IO) {
      whitelistDao.getByPackageName(packageName)?.toDomain()
    }

  suspend fun canKill(packageName: String) = withContext(Dispatchers.IO) {
    whitelistDao.getByPackageName(packageName)?.canKill ?: true
  }

  suspend fun setKilling(packageName: String, canKill: Boolean) {
    withContext(Dispatchers.IO) {
      with(whitelistDao) {
        val oldEntry = getByPackageName(packageName)
        oldEntry?.let {
          update(
            WhitelistEntry(
              packageName = packageName,
              canKill = canKill,
              canLaunch = oldEntry.canLaunch,
              canShow = oldEntry.canShow
            )
          )
        } ?: run {
          insert(
            WhitelistEntry(packageName = packageName, canKill = canKill)
          )
        }
      }
    }
  }

  suspend fun canLaunch(packageName: String) = withContext(Dispatchers.IO) {
    whitelistDao.getByPackageName(packageName)?.canLaunch ?: true
  }

  suspend fun setLaunching(packageName: String, canLaunch: Boolean) {
    withContext(Dispatchers.IO) {
      with(whitelistDao) {
        val oldEntry = getByPackageName(packageName)
        oldEntry?.let {
          update(
            WhitelistEntry(
              packageName = packageName,
              canKill = oldEntry.canKill,
              canLaunch = canLaunch,
              canShow = oldEntry.canShow
            )
          )
        } ?: run {
          insert(
            WhitelistEntry(packageName = packageName, canLaunch = canLaunch)
          )
        }
      }
    }
  }

  suspend fun canShow(packageName: String) = withContext(Dispatchers.IO) {
    whitelistDao.getByPackageName(packageName)?.canShow ?: true
  }

  suspend fun setShowing(packageName: String, canShow: Boolean) {
    withContext(Dispatchers.IO) {
      with(whitelistDao) {
        val oldEntry = getByPackageName(packageName)
        oldEntry?.let {
          update(
            WhitelistEntry(
              packageName = packageName,
              canKill = oldEntry.canKill,
              canLaunch = oldEntry.canLaunch,
              canShow = canShow
            )
          )
        } ?: run {
          insert(
            WhitelistEntry(packageName = packageName, canShow = canShow)
          )
        }
      }
    }
  }
  
  suspend fun setDefaultShowing(packageName: String, canShow: Boolean) {
    withContext(Dispatchers.IO) {
      with(whitelistDao) {
        if (getByPackageName(packageName) == null)
          insert(WhitelistEntry(packageName = packageName, canShow = canShow))
      }
    }
  }
}