package com.tymwitko.recents.recentapps.pinned.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.common.exceptions.AppNotLaunchedException
import com.tymwitko.recents.common.ui.toImageBitmap

@Composable
fun PinnedAppItem(
  app: App,
  iconSize: Dp,
  launchApp: (App) -> Unit
) {
  var isRunning: Boolean by rememberSaveable { mutableStateOf(app.isRunning) }
  LaunchedEffect(isRunning) { isRunning = app.isRunning }
  Box(
    modifier = Modifier.padding(16.dp),
    contentAlignment = Alignment.TopEnd
  ) {
    Image(
      modifier = Modifier
        .width(iconSize)
        .height(iconSize)
        .pointerInput(Unit) {
          detectTapGestures(
            onTap = {
              try {
                launchApp(app)
                app.isRunning = true
                isRunning = true
              } catch (_: AppNotLaunchedException) {
              }
            }
          )
        },
      bitmap = app.icon ?: painterResource(android.R.drawable.ic_menu_gallery).toImageBitmap(
        LocalDensity.current,
        LocalLayoutDirection.current
      ),
      contentDescription = null
    )
    if (isRunning) {
      Image(
        bitmap = painterResource(android.R.drawable.presence_online).toImageBitmap(
          LocalDensity.current,
          LocalLayoutDirection.current
        ),
        contentDescription = null
      )
    }
  }
}
