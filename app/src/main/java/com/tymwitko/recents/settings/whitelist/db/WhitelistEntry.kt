package com.tymwitko.recents.settings.whitelist.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "whitelist")
data class WhitelistEntry(
  @PrimaryKey(autoGenerate = false)
  val packageId: String,
  
  @ColumnInfo(name = "packageName")
  val packageName: String,
  
  @ColumnInfo(name = "user")
  val user: Int,

  @ColumnInfo(name = "can_launch")
  var canLaunch: Boolean = true,

  @ColumnInfo(name = "can_kill")
  var canKill: Boolean = true,

  @ColumnInfo(name = "can_show")
  var canShow: Boolean = true
) {
  constructor(ps: PackageSettings) : this(
    ps.getId(),
    ps.packageName,
    ps.user,
    ps.canLaunch,
    ps.canKill,
    ps.canShow
  )
  
  constructor(
    packageName: String,
    isWorkApp: Boolean,
    canLaunch: Boolean = true,
    canKill: Boolean = true,
    canShow: Boolean = true
  ) : this(
    packageId = packageName + if (isWorkApp) 10 else 0,
    packageName = packageName,
    user = if (isWorkApp) 10 else 0,
    canLaunch = canLaunch,
    canKill = canKill,
    canShow = canShow
  )
}

fun WhitelistEntry.toDomain() = PackageSettings(packageName, user, canLaunch, canKill, canShow)
