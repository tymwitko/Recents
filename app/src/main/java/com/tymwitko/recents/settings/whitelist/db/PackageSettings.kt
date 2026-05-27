package com.tymwitko.recents.settings.whitelist.db

data class PackageSettings(
  val packageName: String,
  val canLaunch: Boolean,
  val canKill: Boolean,
  val canShow: Boolean,
)