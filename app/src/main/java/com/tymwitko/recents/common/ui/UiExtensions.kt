package com.tymwitko.recents.common.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Indication
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.ripple
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

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

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalComposeUiApi
@Suppress("Deprecation")
fun Modifier.dpadFocusable(
  onClick: () -> Unit,
  borderWidth: Dp = 4.dp,
  unfocusedBorderColor: Color = Color(0x00f39c12),
  focusedBorderColor: Color = Color(0xfff39c12),
  indication: Indication? = null,
  scrollPadding: Rect = Rect.Zero,
  isDefault: Boolean = false
) = composed {
  val focusRequester = remember { FocusRequester() }
  val bringIntoViewRequester = remember { BringIntoViewRequester() }
  val boxInteractionSource = remember { MutableInteractionSource() }
  val isItemFocused by boxInteractionSource.collectIsFocusedAsState()
  val animatedBorderColor by animateColorAsState(
    targetValue =
      if (isItemFocused) focusedBorderColor
      else unfocusedBorderColor
  )
  var previousFocus: FocusInteraction.Focus? by remember {
    mutableStateOf(null)
  }
  var previousPress: PressInteraction.Press? by remember {
    mutableStateOf(null)
  }
  val scope = rememberCoroutineScope()
  var boxSize by remember {
    mutableStateOf(IntSize(0, 0))
  }
  val inputMode = LocalInputModeManager.current

  LaunchedEffect(inputMode.inputMode) {
    when (inputMode.inputMode) {
      InputMode.Keyboard -> {
        if (isDefault) {
          focusRequester.requestFocus()
        }
      }
      InputMode.Touch -> {}
    }
  }
  LaunchedEffect(isItemFocused) {
    previousPress?.let {
      if (!isItemFocused) {
        boxInteractionSource.emit(
          PressInteraction.Release(
            press = it
          )
        )
      }
    }
  }

  if (inputMode.inputMode == InputMode.Touch)
    this.clickable(
      interactionSource = boxInteractionSource,
      indication = indication ?: ripple()
    ) {
      onClick()
    }
  else
    this
      .bringIntoViewRequester(bringIntoViewRequester)
      .onSizeChanged {
        boxSize = it
      }
      .indication(
        interactionSource = boxInteractionSource,
        indication = indication ?: ripple()
      )
      .onFocusChanged { focusState ->
        if (focusState.isFocused) {
          val newFocusInteraction = FocusInteraction.Focus()
          scope.launch {
            boxInteractionSource.emit(newFocusInteraction)
          }
          scope.launch {
            val visibilityBounds = Rect(
              left = -1f * scrollPadding.left,
              top = -1f * scrollPadding.top,
              right = boxSize.width + scrollPadding.right,
              bottom = boxSize.height + scrollPadding.bottom
            )
            bringIntoViewRequester.bringIntoView(visibilityBounds)
          }
          previousFocus = newFocusInteraction
        } else {
          previousFocus?.let {
            scope.launch {
              boxInteractionSource.emit(FocusInteraction.Unfocus(it))
            }
          }
        }
      }
      .onKeyEvent {
        if (!listOf(Key.DirectionCenter, Key.Enter).contains(it.key)) {
          return@onKeyEvent false
        }
        when (it.type) {
          KeyEventType.KeyDown -> {
            val press =
              PressInteraction.Press(
                pressPosition = Offset(
                  x = boxSize.width / 2f,
                  y = boxSize.height / 2f
                )
              )
            scope.launch {
              boxInteractionSource.emit(press)
            }
            previousPress = press
            true
          }
          KeyEventType.KeyUp -> {
            previousPress?.let { previousPress ->
              onClick()
              scope.launch {
                boxInteractionSource.emit(
                  PressInteraction.Release(
                    press = previousPress
                  )
                )
              }
            }
            true
          }
          else -> {
            false
          }
        }
      }
      .focusRequester(focusRequester)
      .focusTarget()
      .border(
        width = borderWidth,
        color = animatedBorderColor
      )
}
