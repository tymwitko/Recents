package com.tymwitko.recents.recentapps

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tymwitko.recents.R
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.common.dataclasses.DumpApp
import com.tymwitko.recents.common.exceptions.AppNotLaunchedException
import com.tymwitko.recents.common.ui.toImageBitmap
import org.koin.androidx.compose.koinViewModel

@Composable
fun RecentAppsItem(
  app: App,
  hasPrivileges: Boolean,
  isSwipeToKill: Boolean,
  // iconSize: Dp,
  // fontSize: TextUnit,
  launchApp: (App) -> Unit,
  showQuickSettings: (String, String, Int, Int) -> Unit,
  viewModel: RecentAppsViewModel = koinViewModel()
) {
  var tileY: Int? by remember { mutableStateOf(null) }
  var isRunning: Boolean by rememberSaveable { mutableStateOf(app.isRunning) }
  var isOnlyRunning: Boolean by rememberSaveable { mutableStateOf(viewModel.isOnlyRunning()) }
  val context = LocalContext.current
  val resources = LocalResources.current
  val defaultIconSize = dimensionResource(R.dimen.icon_dimension).value.toInt()

  fun killByPackageName(packageName: String, context: Context, resources: Resources) {
    viewModel.killByPackageName(
      packageName,
      onSucc = {
        Log.d("TAG", "Killed $packageName")
        Toast.makeText(
          context,
          resources.getString(R.string.killed_app, packageName),
          Toast.LENGTH_SHORT
        ).show()
        isRunning = false
        app.isRunning = false
        if (isOnlyRunning) viewModel.removeAppFromList(app)
      },
      onError =  {
        Log.d("TAG", "Failed to kill $packageName")
        Toast.makeText(
          context,
          resources.getString(R.string.failed_to_kill_app, packageName),
          Toast.LENGTH_SHORT
        ).show()
      }
    )
  }
  
  val swipeToDismissBoxState = rememberSwipeToDismissBoxState(
    SwipeToDismissBoxValue.Settled,
    SwipeToDismissBoxDefaults.positionalThreshold
  )

  SwipeToDismissBox(
    state = swipeToDismissBoxState,
    backgroundContent = {},
    enableDismissFromEndToStart = isSwipeToKill,
    enableDismissFromStartToEnd = isSwipeToKill,
    onDismiss = {
      killByPackageName(app.packageName, context, resources)
      viewModel.removeAppFromList(app)
    }
  ) {
    Row(
      modifier = Modifier
        .padding(4.dp)
        .border(width = 1.dp, color = Color.DarkGray, shape = RoundedCornerShape(12.dp))
        .padding(16.dp)
        .pointerInput(Unit) {
          detectTapGestures(
            onTap = {
              try {
                launchApp(app)
                app.isRunning = true
                isRunning = true
              } catch (_: AppNotLaunchedException) {
              }
            },
            onLongPress = {
              showQuickSettings(
                app.packageName,
                app.name,
                it.x.toInt(),
                it.y.toInt() + (tileY ?: 0)
              )
            }
          )
        }
        .onGloballyPositioned {
          tileY = it.positionInRoot().y.toInt()
        },
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        contentAlignment = Alignment.BottomEnd
      ) {
        Box(
          contentAlignment = Alignment.TopEnd
        ) {
          Image(
            modifier = Modifier
              .width(viewModel.getIconSize(defaultIconSize))
              .height(viewModel.getIconSize(defaultIconSize)),
            bitmap = app.icon ?: painterResource(android.R.drawable.ic_menu_gallery).toImageBitmap(
              LocalDensity.current,
              LocalLayoutDirection.current
            ),
            contentDescription = null
          )
          if (isRunning && !isOnlyRunning) {
            Image(
              bitmap = painterResource(android.R.drawable.presence_online).toImageBitmap(
                LocalDensity.current,
                LocalLayoutDirection.current
              ),
              contentDescription = null
            )
          }
        }
        if ((app as? DumpApp)?.isWorkApp == true) {
          Surface(
            modifier = Modifier
              .background(Color.Transparent)
              .size(viewModel.getIconSize(defaultIconSize) / 2)
              .padding(top = 4.dp, start = 4.dp),
            shape = CircleShape
          ) {
            Image(
              modifier = Modifier
                .background(Color.White)
                .padding(4.dp),
              bitmap = painterResource(R.drawable.work_profile).toImageBitmap(
                LocalDensity.current,
                LocalLayoutDirection.current
              ),
              contentDescription = null
            )
          }
        }
      }
      Column(
        modifier = Modifier
          .padding(16.dp)
          .weight(1f)
      ) {
        Text(
          text = app.name,
          color = MaterialTheme.colorScheme.onBackground,
          fontSize = viewModel.getFontSize()
        )
        Text(
          text = app.packageName,
          color = MaterialTheme.colorScheme.onBackground,
          fontSize = viewModel.getFontSize()
        )
      }
      if (hasPrivileges && !isSwipeToKill) Button(
        onClick = { killByPackageName(app.packageName, context, resources) }
      ) {
        Text(text = stringResource(R.string.kill).uppercase())
      }
    }
  }
}
