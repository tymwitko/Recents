package com.tymwitko.recents.settings.whitelist.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.tymwitko.recents.R
import com.tymwitko.recents.common.ui.toImageBitmap
import com.tymwitko.recents.settings.whitelist.WhitelistSettingsData

@Composable
fun WhitelistItem(
  name: String,
  packageName: String,
  icon: ImageBitmap?,
  showKillCheck: Boolean,
  fontSize: TextUnit,
  iconSize: Dp,
  whitelistLaunch: (String, Boolean) -> Unit,
  whitelistKill: (String, Boolean) -> Unit,
  whitelistShow: (String, Boolean) -> Unit,
  settings: MutableLiveData<WhitelistSettingsData>?,
  lifecycleOwner: LifecycleOwner?
) {
  var launchChecked by rememberSaveable { mutableStateOf(true) }
  var killChecked by rememberSaveable { mutableStateOf(true) }
  var showChecked by rememberSaveable { mutableStateOf(true) }
  var expanded by rememberSaveable { mutableStateOf(false) }
  val extraPadding by animateDpAsState(
    if (expanded) 12.dp else 0.dp,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMedium
    )
  )

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

  lifecycleOwner?.let {
    settings?.observe(it) {
      onLaunchChecked(it.canLaunch)
      onKillChecked(it.canKill)
      onShowChecked(it.canShow)
    }
  }
  Column(
    modifier = Modifier
      .padding(4.dp)
      .padding(bottom = extraPadding.coerceAtLeast(0.dp))
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
        bitmap = icon ?: painterResource(android.R.drawable.ic_menu_gallery).toImageBitmap(
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
        Text(text = name, color = MaterialTheme.colorScheme.onBackground, fontSize = fontSize)
        Text(text = packageName, color = MaterialTheme.colorScheme.onBackground, fontSize = fontSize)
      }
      ShowMoreButt(expanded) {
        expanded = !expanded
      }
    }
      if (expanded) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center
        ) {
          Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = stringResource(R.string.launch),
              color = MaterialTheme.colorScheme.onBackground,
              fontSize = fontSize
            )
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
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            if (showKillCheck) {
              Text(
                text = stringResource(R.string.kill),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = fontSize
              )
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
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = stringResource(R.string.show),
              color = MaterialTheme.colorScheme.onBackground,
              fontSize = fontSize
            )
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
  }
}
