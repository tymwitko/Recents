package com.tymwitko.recents.whitelist.ui

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.tymwitko.recents.R
import com.tymwitko.recents.whitelist.WhitelistSettings

@Composable
fun WhitelistItem(
  name: String,
  packageName: String,
  icon: ImageBitmap,
  showKillCheck: Boolean,
  whitelistLaunch: (String, Boolean) -> Unit,
  whitelistKill: (String, Boolean) -> Unit,
  whitelistShow: (String, Boolean) -> Unit,
  settings: MutableLiveData<WhitelistSettings>?,
  lifecycleOwner: LifecycleOwner
) {
  var launchChecked by rememberSaveable { mutableStateOf(true) }
  var killChecked by rememberSaveable { mutableStateOf(true) }
  var showChecked by rememberSaveable { mutableStateOf(true) }

  fun onLaunchChecked(isChecked: Boolean) {
    if (isChecked == launchChecked) return
    whitelistLaunch(packageName, isChecked)
    launchChecked = isChecked
  }

  fun onKillChecked(isChecked: Boolean) {
    if (isChecked == killChecked) return
    whitelistKill(packageName, isChecked)
    killChecked = isChecked
  }

  fun onShowChecked(isChecked: Boolean) {
    if (isChecked == showChecked) return
    whitelistShow(packageName, isChecked)
    showChecked = isChecked
  }

  settings?.observe(lifecycleOwner) {
    onLaunchChecked(it.canLaunch)
    onKillChecked(it.canKill)
    onShowChecked(it.canShow)
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
    Column(
      modifier = Modifier.padding(2.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(text = stringResource(R.string.launch), color = MaterialTheme.colorScheme.onBackground)
      Checkbox(
        checked = launchChecked,
        onCheckedChange = { isChecked ->
          onLaunchChecked(isChecked)
          settings?.value?.let {
            settings.postValue(
              it.apply {
                canLaunch = isChecked
              }
            )
          }
        }
      )
    }
    Column(
      modifier = Modifier.padding(2.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      if (showKillCheck) {
        Text(text = stringResource(R.string.kill), color = MaterialTheme.colorScheme.onBackground)
        Checkbox(
          checked = killChecked,
          onCheckedChange = { isChecked ->
            onKillChecked(isChecked)
            settings?.value?.let {
              settings.postValue(
                it.apply {
                  canKill = isChecked
                }
              )
            }
          }
        )
      }
    }
    Column(
      modifier = Modifier.padding(2.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(text = stringResource(R.string.show), color = MaterialTheme.colorScheme.onBackground)
      Checkbox(
        checked = showChecked,
        onCheckedChange = { isChecked ->
          onShowChecked(isChecked)
          settings?.value?.let {
            settings.postValue(
              it.apply {
                canShow = isChecked
              }
            )
          }
        }
      )
    }
  }
}
