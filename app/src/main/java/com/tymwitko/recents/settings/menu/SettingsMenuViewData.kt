package com.tymwitko.recents.settings.menu

import androidx.compose.ui.graphics.ImageBitmap

data class SettingsMenuViewData(
  val name: String,
  val icon: ImageBitmap,
  val route: String?
)