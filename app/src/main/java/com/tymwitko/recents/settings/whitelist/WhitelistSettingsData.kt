package com.tymwitko.recents.settings.whitelist

import com.tymwitko.recents.settings.whitelist.db.PackageSettings

data class WhitelistSettingsData(
  var canLaunch: Boolean = true,
  var canKill: Boolean = true,
  var canShow: Boolean = true
) {
  constructor(ps: PackageSettings) : this(ps.canLaunch, ps.canKill, ps.canShow)
}
