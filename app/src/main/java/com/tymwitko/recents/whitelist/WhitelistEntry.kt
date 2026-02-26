package com.tymwitko.recents.whitelist

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "whitelist")
data class WhitelistEntry(
  @PrimaryKey(autoGenerate = false)
  val packageName: String,

  @ColumnInfo(name = "can_launch")
  var canLaunch: Boolean = true,

  @ColumnInfo(name = "can_kill")
  var canKill: Boolean = true
)
