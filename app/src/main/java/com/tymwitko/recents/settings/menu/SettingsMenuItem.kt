package com.tymwitko.recents.settings.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tymwitko.recents.R

@Composable
fun SettingsMenuItem(
  text: String,
  icon: ImageBitmap,
  route: String?,
  navController: NavHostController
) {
  Row(
    modifier = Modifier
      .fillMaxHeight()
      .padding(4.dp)
      .border(width = 1.dp, color = Color.DarkGray, shape = RoundedCornerShape(12.dp))
      .padding(16.dp)
      .pointerInput(Unit) {
        detectTapGestures(
          onTap = { route?.let { it1 -> navController.navigate(it1) } }
        )
      },
    verticalAlignment = Alignment.CenterVertically
  ) {
    Image(
      modifier = Modifier
        .width(dimensionResource(R.dimen.icon_dimension))
        .height(dimensionResource(R.dimen.icon_dimension)),
      bitmap = icon,
      contentDescription = null
    )
    Column(
      modifier = Modifier
        .padding(16.dp)
        .weight(1f)
    ) {
      Text(text = text, color = MaterialTheme.colorScheme.onBackground)
    }
  }
}
