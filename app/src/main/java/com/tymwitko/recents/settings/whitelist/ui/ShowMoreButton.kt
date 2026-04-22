package com.tymwitko.recents.settings.whitelist.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun ShowMoreButt(expanded: Boolean, onClick: () -> Unit){
  IconButton (
    onClick = onClick
  ) {
    androidx.compose.material3.Icon(
      imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
      contentDescription = "Show ${if (expanded) "less" else "more"}",
      tint = MaterialTheme.colorScheme.onBackground
    )
    //Text("Show ${if (expanded.value) "less" else "more"}")
  }
}
