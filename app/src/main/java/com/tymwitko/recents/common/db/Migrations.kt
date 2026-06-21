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
      db.execSQL(
        "CREATE TABLE IF NOT EXISTS `pinned`" +
          "(`packageId` TEXT NOT NULL, `packageName` TEXT NOT NULL," +
          "`user` INTEGER NOT NULL, PRIMARY KEY(`packageId`))"
      )
    }
  }
  val MIG_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL(
        """
          CREATE TABLE whitelist_new (
            packageId TEXT NOT NULL PRIMARY KEY,
            packageName TEXT NOT NULL,
            user INTEGER NOT NULL,
            can_launch INTEGER NOT NULL,
            can_kill INTEGER NOT NULL,
            can_show INTEGER NOT NULL
          )
        """.trimIndent()
      )

      db.execSQL(
        """
          INSERT INTO whitelist_new (packageId, packageName, user, can_launch, can_kill, can_show)
            SELECT
            packageName || '0' AS packageId,
            packageName,
            '0' AS user,
            can_launch,
            can_kill,
            can_show
          FROM (
            SELECT
              packageName,
              can_launch,
              can_kill,
              can_show
            FROM whitelist
          )
        """.trimIndent()
      )

      db.execSQL("DROP TABLE whitelist")
      db.execSQL("ALTER TABLE whitelist_new RENAME TO whitelist")
    }
  }
}
