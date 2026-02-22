package com.tymwitko.recents.whitelist

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WhitelistRepository(private val whitelistDao: WhitelistDao) {
  suspend fun canKill(packageName: String) = withContext(Dispatchers.IO) {
    whitelistDao.getByPackageName(packageName)?.canKill ?: true
  }

  suspend fun setKilling(packageName: String, canKill: Boolean) {
    // whitelistDao.insert()
  }

  suspend fun canLaunch(packageName: String) = withContext(Dispatchers.IO) {
    whitelistDao.getByPackageName(packageName)?.canLaunch ?: true
  }

  suspend fun setLaunching(packageName: String, canLaunch: Boolean) {

  }
}