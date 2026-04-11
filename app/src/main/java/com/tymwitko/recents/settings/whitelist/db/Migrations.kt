package com.tymwitko.recents.settings.whitelist.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
  val MIG_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("ALTER TABLE whitelist ADD COLUMN can_show INTEGER DEFAULT 1 NOT NULL")
    }
  }
}