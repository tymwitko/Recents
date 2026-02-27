package com.tymwitko.recents.dataclasses

import androidx.lifecycle.MutableLiveData

data class SettingItem(
  val app: App,
  val settings: MutableLiveData<Pair<Boolean, Boolean>>?,
)
