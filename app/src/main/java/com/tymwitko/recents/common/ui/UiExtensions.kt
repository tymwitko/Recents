package com.tymwitko.recents.common.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

fun Modifier.clearFocusOnKeyboardDismiss(): Modifier = composed {
  var isFocused by remember { mutableStateOf(false) }
  var keyboardAppearedSinceLastFocused by remember { mutableStateOf(false) }

  if (isFocused) {
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val focusManager = LocalFocusManager.current
    LaunchedEffect(imeVisible) {
      if (imeVisible) {
        keyboardAppearedSinceLastFocused = true
      } else if (keyboardAppearedSinceLastFocused) {
        focusManager.clearFocus()
      }
    }
  }
  onFocusEvent {
    if (isFocused != it.isFocused) {
      isFocused = it.isFocused
      if (isFocused) keyboardAppearedSinceLastFocused = false
    }
  }
}

fun Painter.toImageBitmap(
  density: Density,
  layoutDirection: LayoutDirection,
): ImageBitmap {
  val bitmap = ImageBitmap(intrinsicSize.width.toInt(), intrinsicSize.height.toInt())
  val canvas = Canvas(bitmap)
  CanvasDrawScope().draw(density, layoutDirection, canvas, intrinsicSize) {
    draw(intrinsicSize)
  }
  return bitmap
}
