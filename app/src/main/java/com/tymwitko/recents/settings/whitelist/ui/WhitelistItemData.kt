package com.tymwitko.recents.settings.whitelist.ui

import androidx.lifecycle.MutableLiveData
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.settings.whitelist.WhitelistSettingsData

data class WhitelistItemData(
  val app: App,
  val settings: MutableLiveData<WhitelistSettingsData>?,
)
