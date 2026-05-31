package com.tymwitko.recents.recentapps.pinned.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pinned")
data class PinnedEntry(
  @PrimaryKey(autoGenerate = false)
  val packageId: String,

  @ColumnInfo(name = "packageName")
  var packageName: String = "",

  @ColumnInfo(name = "user")
  var user: Int = 0,
) {
  constructor(pinned: PinnedAppDetails) : this(pinned.getId(), pinned.packageName, pinned.user)
}

fun PinnedEntry.toDomain() = PinnedAppDetails(packageName, user)
