package com.tymwitko.recents.whitelist

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "whitelist")
class WhitelistEntry {
  @PrimaryKey(autoGenerate = false)
  lateinit var packageName: String

  @ColumnInfo(name = "can_launch")
  var canLaunch: Boolean = true

  @ColumnInfo(name = "can_kill")
  var canKill: Boolean = true
}
