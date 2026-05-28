package com.tymwitko.recents.common.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tymwitko.recents.recentapps.pinned.db.PinnedEntry
import com.tymwitko.recents.settings.whitelist.db.WhitelistEntry

@Database(entities = [WhitelistEntry::class, PinnedEntry::class], version = 3, exportSchema = false)
abstract class RecentsDatabase : RoomDatabase() {
  abstract fun recentsDao(): RecentsDao
}
