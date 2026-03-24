package com.tymwitko.recents.recentapps.quicksettings

import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.tymwitko.recents.whitelist.WhitelistSettings

@Composable
fun QuickSettingsItem(
  modifier: Modifier = Modifier,
  text: String,
  settings: MutableLiveData<WhitelistSettings>?,
  lifecycleOwner: LifecycleOwner,
  settingType: WhitelistSettingType,
  onCheck: (Boolean) -> Unit
) {
  fun getFieldForType(sets: WhitelistSettings) = when (settingType) {
    WhitelistSettingType.LAUNCH -> sets.canLaunch
    WhitelistSettingType.KILL -> sets.canKill
    WhitelistSettingType.SHOW -> sets.canShow
  }
  
  var checked by rememberSaveable {
    mutableStateOf(settings?.value?.let { getFieldForType(it) } ?: true)
  }
  settings?.observe(lifecycleOwner) {
    Log.d("TAG", "settings: $settingType, ${it.canLaunch}, ${it.canKill}, ${it.canShow}")
    getFieldForType(it).let { settingVal ->
      if (settingVal != checked) {
        checked = settingVal

        Log.d("TAG", "checked set to $checked for $text")
        onCheck(checked)
      }
    }
  }
  Row(modifier.padding(8.dp)) {
    Text(text = text, color = MaterialTheme.colorScheme.onBackground)
    Checkbox(
      checked = checked,
      onCheckedChange = { isChecked ->
        onCheck(isChecked)
        checked = isChecked
        settings?.value?.let {
          settings.postValue(
            it.apply {
              when (settingType) {
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
