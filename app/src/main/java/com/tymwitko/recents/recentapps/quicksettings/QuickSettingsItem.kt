package com.tymwitko.recents.recentapps.quicksettings

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.tymwitko.recents.settings.whitelist.WhitelistSettingsData

@Composable
fun QuickSettingsItem(
  modifier: Modifier = Modifier,
  text: String,
  settings: MutableLiveData<WhitelistSettingsData>?,
  lifecycleOwner: LifecycleOwner,
  settingType: WhitelistSettingType?,
  triggerHandler: (Boolean) -> Unit
) {
  fun getFieldForType(sets: WhitelistSettingsData) = when (settingType) {
    WhitelistSettingType.LAUNCH -> sets.canLaunch
    WhitelistSettingType.KILL -> sets.canKill
    WhitelistSettingType.SHOW -> sets.canShow
    null -> null
  }
  
  var checked by rememberSaveable {
    mutableStateOf(settings?.value?.let { getFieldForType(it) } ?: true)
  }
  settings?.observe(lifecycleOwner) {
    getFieldForType(it)?.let { settingVal ->
      if (settingVal != checked) {
        checked = settingVal
        triggerHandler(checked)
      }
    }
  }
  Row(
    modifier
      .border(width = 1.dp, color = Color.DarkGray, shape = CutCornerShape(0.dp))
      .fillMaxWidth()
      .padding(12.dp)
      .pointerInput(Unit) {
        detectTapGestures(
          onTap = {
            if (settingType == null) triggerHandler(true)
          }
        )
      },
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(
      modifier = Modifier.align(Alignment.CenterVertically),
      text = text,
      color = MaterialTheme.colorScheme.onBackground
    )
    settingType?.let { type ->
      Checkbox(
        modifier = Modifier.align(Alignment.CenterVertically),
        checked = checked,
        onCheckedChange = { isChecked ->
          triggerHandler(isChecked)
          checked = isChecked
          settings?.value?.let {
            settings.postValue(
              it.apply {
                when (type) {
                  WhitelistSettingType.LAUNCH -> canLaunch = isChecked
                  WhitelistSettingType.KILL -> canKill = isChecked
                  WhitelistSettingType.SHOW -> canShow = isChecked
                }
              }
            )
          }
        }
      )
    }
  }
}
