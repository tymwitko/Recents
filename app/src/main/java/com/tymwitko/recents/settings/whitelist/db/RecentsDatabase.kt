package com.tymwitko.recents.settings.whitelist.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [WhitelistEntry::class], version = 2, exportSchema = false)
abstract class RecentsDatabase : RoomDatabase() {
  abstract fun whitelistDao(): WhitelistDao
}
