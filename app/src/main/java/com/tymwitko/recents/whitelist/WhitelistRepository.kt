package com.tymwitko.recents.whitelist

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WhitelistRepository(private val whitelistDao: WhitelistDao) {
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
              canLaunch = oldEntry.canLaunch
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
              canLaunch = canLaunch
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
}