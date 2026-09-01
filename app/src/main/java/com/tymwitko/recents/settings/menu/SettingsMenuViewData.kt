package com.tymwitko.recents.settings.menu

import androidx.compose.ui.graphics.painter.Painter

data class SettingsMenuViewData(
  val name: String,
  val icon: Painter,
  val route: String
)
