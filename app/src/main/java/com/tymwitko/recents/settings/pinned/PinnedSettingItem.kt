package com.tymwitko.recents.settings.pinned

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.tymwitko.recents.R
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.common.ui.toImageBitmap

@Composable
fun PinnedSettingItem(
  app: App,
  iconSize: Dp,
  fontSize: TextUnit,
  isPinned: Boolean,
  pinApp: (App) -> Unit
) {
  fun onPinnedChecked(isChecked: Boolean) {
    if (isChecked == isPinned) return
    pinApp(app)
  }

  Column(
    modifier = Modifier
      .padding(4.dp)
      .border(width = 1.dp, color = Color.DarkGray, shape = RoundedCornerShape(12.dp))
  ) {
    Row(
      modifier = Modifier
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Image(
        modifier = Modifier
          .width(iconSize)
          .height(iconSize),
        bitmap = app.icon ?: painterResource(android.R.drawable.ic_menu_gallery).toImageBitmap(
          LocalDensity.current,
          LocalLayoutDirection.current
        ),
        contentDescription = null
      )
      Column(
        modifier = Modifier
          .padding(16.dp)
          .weight(1f)
      ) {
        Text(text = app.name, color = MaterialTheme.colorScheme.onBackground, fontSize = fontSize)
        Text(
          text = app.packageName,
          color = MaterialTheme.colorScheme.onBackground,
          fontSize = fontSize
        )
      }
      Column(
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = stringResource(R.string.pin),
          color = MaterialTheme.colorScheme.onBackground,
          fontSize = fontSize
        )
        Checkbox(
          checked = isPinned,
          onCheckedChange = { isChecked ->
            onPinnedChecked(isChecked)
          }
        )
      }
    }
  }
}
