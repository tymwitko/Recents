package com.tymwitko.recents.recentapps.pinned.db

data class PinnedAppDetails(
  val packageName: String,
  val user: Int
) {
  fun getId() = packageName + user.toString()
}
