package com.tymwitko.recents.common.dataclasses

data class ActiveApp(
  val packageName: String,
  val isRunning: Boolean,
  val lastActive: Long,
  val isWorkApp: Boolean
)
