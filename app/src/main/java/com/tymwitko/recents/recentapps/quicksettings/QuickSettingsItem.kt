package com.tymwitko.recents.recentapps.quicksettings

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tymwitko.recents.settings.whitelist.WhitelistSettingsData
import kotlinx.coroutines.flow.StateFlow

@Composable
fun QuickSettingsItem(
  modifier: Modifier = Modifier,
  text: String,
  settings: StateFlow<WhitelistSettingsData?>,
  settingType: WhitelistSettingType?,
  fontSize: TextUnit,
  onCheck: (Boolean) -> Unit
) {
  fun getFieldForType(sets: WhitelistSettingsData?) = when (settingType) {
    WhitelistSettingType.LAUNCH -> sets?.canLaunch
    WhitelistSettingType.KILL -> sets?.canKill
    WhitelistSettingType.SHOW -> sets?.canShow
    null -> null
  }
  val sets by settings.collectAsStateWithLifecycle()
  SideEffect {
    Log.d("QuickSettingsItem", "sets now: $sets")
  } 
  var checked = getFieldForType(sets)
  LaunchedEffect(sets) {
    getFieldForType(sets).let { settingVal ->
      if (settingVal != checked) {
        checked = settingVal
      }
    }
  }
  Row(
    modifier
      .border(width = 1.dp, color = Color.DarkGray, shape = CutCornerShape(0.dp))
      .fillMaxWidth()
      .padding(vertical = if (settingType != null) 0.dp else 8.dp)
      .padding(12.dp)
      .pointerInput(Unit) {
        detectTapGestures(
          onTap = {
            if (settingType == null) onCheck(true)
          }
        )
      },
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(
      modifier = Modifier.align(Alignment.CenterVertically),
      text = text,
      color = MaterialTheme.colorScheme.onBackground,
      fontSize = fontSize
    )
    checked?.let {
      Checkbox(
        modifier = Modifier.align(Alignment.CenterVertically),
        checked = it,
        onCheckedChange = { isChecked ->
          onCheck(isChecked)
          checked = isChecked
        }
      )
    }
  }
}
