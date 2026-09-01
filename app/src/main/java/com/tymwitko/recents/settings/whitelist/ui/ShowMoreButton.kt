package com.tymwitko.recents.settings.whitelist.ui

import androidx.compose.foundation.Image
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.tymwitko.recents.R

@Composable
fun ShowMoreButt(expanded: Boolean, onClick: () -> Unit){
  IconButton (
    onClick = onClick
  ) {
    Image(
      painter = painterResource(if (expanded) R.drawable.collapse else R.drawable.expand),
      contentDescription = "Show ${if (expanded) "less" else "more"}",
      colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground)
    )
  }
}
