package com.tymwitko.recents.common.dataclasses

import androidx.compose.ui.graphics.ImageBitmap

data class App(
  val name: String,
  val packageName: String,
  val icon: ImageBitmap
)
