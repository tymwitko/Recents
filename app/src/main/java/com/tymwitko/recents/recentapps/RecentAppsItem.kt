package com.tymwitko.recents.recentapps

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.tymwitko.recents.R

@Composable
fun RecentAppsItem(
  name: String,
  packageName: String,
  icon: ImageBitmap,
  fontSize: TextUnit,
  iconSize: Dp,
  launchApp: (String) -> Unit,
  killApp: (String) -> Unit,
  showQuickSettings: (String, String, Int, Int) -> Unit,
  hasPrivileges: Boolean
) {
  var tileY: Int? by remember { mutableStateOf(null) }
  Row(
    modifier = Modifier
      .padding(4.dp)
      .border(width = 1.dp, color = Color.DarkGray, shape = RoundedCornerShape(12.dp))
      .padding(16.dp)
      .pointerInput(Unit) {
        detectTapGestures(
          onTap = { launchApp(packageName) },
          onLongPress = { showQuickSettings(packageName, name, it.x.toInt(), it.y.toInt() + (tileY ?: 0)) }
        )
      }
      .onGloballyPositioned {
        tileY = it.positionInRoot().y.toInt()
      },
    verticalAlignment = Alignment.CenterVertically
  ) {
    Image(
      modifier = Modifier
        .width(iconSize)
        .height(iconSize),
      bitmap = icon,
      contentDescription = null
    )
    Column(
      modifier = Modifier
        .padding(16.dp)
        .weight(1f)
    ) {
      Text(text = name, color = MaterialTheme.colorScheme.onBackground, fontSize = fontSize)
      Text(text = packageName, color = MaterialTheme.colorScheme.onBackground, fontSize = fontSize)
    }
    if (hasPrivileges) Button(onClick = { killApp(packageName) }) {
      Text(text = stringResource(R.string.kill).uppercase())
    }
  }
}
