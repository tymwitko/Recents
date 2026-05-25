package com.tymwitko.recents.common.dataclasses

import android.content.ComponentName

data class ActiveApp(
  val packageName: String,
  val isRunning: Boolean,
  val lastActive: Long,
  val isWorkApp: Boolean,
  val componentName: ComponentName
)
