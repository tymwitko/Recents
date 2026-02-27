package com.tymwitko.recents.ui.compost

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import com.tymwitko.recents.R

@Composable
fun WhitelistItem(
  name: String,
  packageName: String,
  icon: ImageBitmap,
  whitelistLaunch: (String, Boolean) -> Unit,
  whitelistKill: (String, Boolean) -> Unit,
  settings: LiveData<Pair<Boolean, Boolean>>?,
) {
  val launchChecked = remember { mutableStateOf(settings?.value?.first ?: true) }
  val killChecked = remember { mutableStateOf(settings?.value?.second ?: true) }

  fun onLaunchChecked(isChecked: Boolean) {
    if (isChecked == launchChecked.value) return
    whitelistLaunch(packageName, isChecked)
    launchChecked.value = isChecked
  }

  fun onKillChecked(isChecked: Boolean) {
    if (isChecked == killChecked.value) return
    whitelistKill(packageName, isChecked)
    killChecked.value = isChecked
  }

  settings?.observeForever {
    onLaunchChecked(it.first)
    onKillChecked(it.second)
  }
  Row(
    modifier = Modifier
      .fillMaxHeight()
      .padding(4.dp)
      .border(width = 1.dp, color = Color.DarkGray, shape = RoundedCornerShape(12.dp))
      .padding(16.dp),
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
      Text(text = name, color = MaterialTheme.colorScheme.onBackground)
      Text(text = packageName, color = MaterialTheme.colorScheme.onBackground)
    }
    Checkbox(
      checked = launchChecked.value,
      onCheckedChange = { isChecked ->
        onLaunchChecked(isChecked)
      }
    )
    Checkbox(
      checked = killChecked.value,
      onCheckedChange = { isChecked ->
        onKillChecked(isChecked)
      }
    )
  }
}
