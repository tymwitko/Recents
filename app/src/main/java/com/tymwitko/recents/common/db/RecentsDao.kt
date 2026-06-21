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
  fun insertToWhitelist(entry: WhitelistEntry)

  @Update
  fun updateWhitelist(entry: WhitelistEntry)

  @Query("SELECT * FROM whitelist WHERE packageName = :packageName")
  fun getFromWhitelistByPackageName(packageName: String?): WhitelistEntry?

  @Insert
  fun insertToPinned(entry: PinnedEntry)

  @Delete
  fun deleteFromPinned(entry: PinnedEntry)

  @get:Query("SELECT * FROM pinned")
  val allPinned: List<PinnedEntry>
}
