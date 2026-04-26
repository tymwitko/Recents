package com.tymwitko.recents.common.dataclasses

import android.os.Parcelable
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class App(
  val name: String,
  val packageName: String,
  val icon: @RawValue ImageBitmap
): Parcelable
