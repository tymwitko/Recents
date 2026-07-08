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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.tymwitko.recents.R
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.common.ui.toImageBitmap
import com.tymwitko.recents.settings.whitelist.WhitelistSettingsData

@Composable
fun WhitelistItem(
  app: App,
  showKillCheck: Boolean,
  fontSize: TextUnit,
  iconSize: Dp,
  whitelistLaunch: (App, Boolean) -> Unit,
  whitelistKill: (App, Boolean) -> Unit,
  whitelistShow: (App, Boolean) -> Unit,
  settings: WhitelistSettingsData?
) {
  var expanded by rememberSaveable { mutableStateOf(false) }
  val extraPadding by animateDpAsState(
    if (expanded) 12.dp else 0.dp,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMedium
    )
  )
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
            checked = settings?.canLaunch ?: true,
            onCheckedChange = { isChecked ->
              whitelistLaunch(app, isChecked)
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
              checked = settings?.canKill ?: true,
              onCheckedChange = { isChecked ->
                whitelistKill(app, isChecked)
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
            checked = settings?.canShow ?: true,
            onCheckedChange = { isChecked ->
              whitelistShow(app, isChecked)
            }
          )
        }
      }
    }
  }
}
