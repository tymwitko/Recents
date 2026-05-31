package com.tymwitko.recents.recentapps.pinned.db

import com.tymwitko.recents.common.dataclasses.App

data class PinnedAppDetails(
  val packageName: String,
  val user: Int
) {
  constructor(app: App) : this(
    app.packageName,
    if (app.isWorkApp) 10 else 0
  )

  fun getId() = packageName + user.toString()

  override fun equals(other: Any?): Boolean {
    return (other as? PinnedAppDetails)?.getId() == getId()
  }

  override fun hashCode(): Int {
    return getId().hashCode()
  }
}
