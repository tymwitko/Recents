package com.tymwitko.recents.whitelist.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface WhitelistDao {
  @Insert
  fun insert(entry: WhitelistEntry)

  @Update
  fun update(entry: WhitelistEntry)

  @Delete
  fun delete(entry: WhitelistEntry)

  @Query("SELECT * FROM whitelist WHERE packageName = :packageName")
  fun getByPackageName(packageName: String?): WhitelistEntry?

  @get:Query("SELECT * FROM whitelist")
  val allApps: List<WhitelistEntry?>?
}
