package com.tymwitko.recents.whitelist.ui

import androidx.lifecycle.MutableLiveData
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.whitelist.WhitelistSettings

data class SettingItem(
  val app: App,
  val settings: MutableLiveData<WhitelistSettings>?,
)
