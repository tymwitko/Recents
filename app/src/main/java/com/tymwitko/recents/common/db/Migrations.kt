package com.tymwitko.recents.common.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
  val MIG_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("ALTER TABLE whitelist ADD COLUMN can_show INTEGER DEFAULT 1 NOT NULL")
    }
  }
  val MIG_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("CREATE TABLE IF NOT EXISTS `pinned`" +
        "(`packageId` TEXT NOT NULL, `packageName` TEXT NOT NULL," +
        "`user` INTEGER NOT NULL, PRIMARY KEY(`packageId`))"
      )
    }
  }
}
