package com.tymwitko.recents.whitelist.ui

import androidx.lifecycle.MutableLiveData
import com.tymwitko.recents.common.dataclasses.App

data class SettingItem(
  val app: App,
  val settings: MutableLiveData<Pair<Boolean, Boolean>>?,
)
