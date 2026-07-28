package com.tymwitko.recents.common.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tymwitko.recents.recentapps.pinned.db.PinnedEntry
import com.tymwitko.recents.settings.whitelist.db.WhitelistEntry

@Dao
interface RecentsDao {
  @Insert
  suspend fun insertToWhitelist(entry: WhitelistEntry)

  @Update
  suspend fun updateWhitelist(entry: WhitelistEntry)

  @Query("SELECT * FROM whitelist WHERE packageId = :packageId")
  suspend fun getFromWhitelistByPackageId(packageId: String): WhitelistEntry?

  @Query("SELECT * FROM whitelist")
  suspend fun getFullWhitelist(): List<WhitelistEntry>

  @Insert
  suspend fun insertToPinned(entry: PinnedEntry)

  @Delete
  suspend fun deleteFromPinned(entry: PinnedEntry)

  @Query("SELECT * FROM pinned")
  suspend fun getAllPinned(): List<PinnedEntry>
}
