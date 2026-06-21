package com.tymwitko.recents.recentapps.pinned.db

import com.tymwitko.recents.common.db.RecentsDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PinnedRepository(private val recentsDao: RecentsDao) {

  suspend fun addPinned(pinnedAppDetails: PinnedAppDetails) {
    withContext(Dispatchers.IO) {
      recentsDao.insertToPinned(
        PinnedEntry(pinnedAppDetails)
      )
    }
  }
  
  suspend fun removePinned(pinnedAppDetails: PinnedAppDetails) {
    withContext(Dispatchers.IO) {
      recentsDao.deleteFromPinned(
        PinnedEntry(pinnedAppDetails)
      )
    }   
  }
  
  suspend fun getAllPinned() =
    withContext(Dispatchers.IO) {
      recentsDao.allPinned.map {
        it.toDomain()
      }
    }
}
