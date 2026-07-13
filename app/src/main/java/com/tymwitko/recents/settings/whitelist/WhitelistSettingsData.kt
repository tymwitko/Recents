package com.tymwitko.recents.settings.whitelist

data class WhitelistSettingsData(
  var canLaunch: Boolean = true,
  var canKill: Boolean = true,
  var canShow: Boolean = true
)
