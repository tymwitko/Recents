package com.tymwitko.recents.recentapps.pinned.db

import com.tymwitko.recents.common.db.RecentsDao

class PinnedRepository(private val recentsDao: RecentsDao) {

  suspend fun addPinned(pinnedAppDetails: PinnedAppDetails) {
    recentsDao.insertToPinned(
      PinnedEntry(pinnedAppDetails)
    )
  }

  suspend fun removePinned(pinnedAppDetails: PinnedAppDetails) {
    recentsDao.deleteFromPinned(
      PinnedEntry(pinnedAppDetails)
    )
  }

  suspend fun getAllPinned() =
    recentsDao.getAllPinned().map {
      it.toDomain()
    }
}
