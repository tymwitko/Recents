package com.tymwitko.recents.dataclasses

import androidx.lifecycle.LiveData

data class SettingItem(
  val app: App,
  val settings: LiveData<Pair<Boolean, Boolean>>?,
)
