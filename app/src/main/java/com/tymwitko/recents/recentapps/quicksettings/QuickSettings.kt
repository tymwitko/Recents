package com.tymwitko.recents.recentapps.quicksettings

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.tymwitko.recents.R
import com.tymwitko.recents.settings.whitelist.WhitelistSettingsData
import kotlinx.coroutines.flow.StateFlow

@Composable
fun QuickSettings(
  packageName: String,
  appName: String,
  posX: Int?,
  posY: Int?,
  settings: StateFlow<WhitelistSettingsData>?,
  hasPrivileges: Boolean,
  whitelistAppLaunch: (String, Boolean) -> Unit,
  whitelistAppKill: (String, Boolean) -> Unit,
  whitelistAppShow: (String, Boolean) -> Unit,
  onDismissRequest: () -> Unit
) {
  Popup(
    popupPositionProvider = object : PopupPositionProvider {
      override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
      ) = IntOffset(
        (posX?.minus(popupContentSize.width / 2)) ?: 0,
        (posY?.minus(popupContentSize.height)) ?: 0
      )
    },
    onDismissRequest = onDismissRequest,
    properties = PopupProperties(focusable = true)
  ) {
    Surface(
      shape = RoundedCornerShape(12.dp)
    ) {
      Column(modifier = Modifier
        .padding(0.dp)
        .width(IntrinsicSize.Max)
        .border(width = 1.dp, color = Color.DarkGray, shape = RoundedCornerShape(12.dp))
      ) {
        Text(
          modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = Color.DarkGray, shape = CutCornerShape(0.dp))
            .padding(12.dp),
          text = appName
        )

        settings?.let { sets ->
          QuickSettingsItem(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.launch),
            settings = sets,
            settingType = WhitelistSettingType.LAUNCH,
            onCheck = {
              whitelistAppLaunch(packageName, it)
            }
          )
          if (hasPrivileges)
            QuickSettingsItem(
              modifier = Modifier.fillMaxWidth(),
              text = stringResource(R.string.kill),
              settings = sets,
              settingType = WhitelistSettingType.KILL,
              onCheck = {
                whitelistAppKill(packageName, it)
              }
            )
          QuickSettingsItem(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.show),
            settings = sets,
            settingType = WhitelistSettingType.SHOW,
            onCheck = {
              whitelistAppShow(packageName, it)
            }
          )
        }
      }
    }
  }
}
