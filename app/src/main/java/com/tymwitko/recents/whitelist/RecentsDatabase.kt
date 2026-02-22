package com.tymwitko.recents.whitelist

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import kotlin.concurrent.Volatile

@Database(entities = [WhitelistEntry::class], version = 1, exportSchema = false)
abstract class RecentsDatabase : RoomDatabase() {
  abstract fun whitelistDao(): WhitelistDao

  companion object {
    @Volatile
    private var INSTANCE: RecentsDatabase? = null

    fun getDatabase(context: Context): RecentsDatabase? {
      if (INSTANCE == null) {
        synchronized(RecentsDatabase::class.java) {
          if (INSTANCE == null) {
            INSTANCE = databaseBuilder(
              context.applicationContext,
              RecentsDatabase::class.java,
              "RecentsDB"
            )
              .build()
          }
        }
      }
      return INSTANCE
    }
  }
}
