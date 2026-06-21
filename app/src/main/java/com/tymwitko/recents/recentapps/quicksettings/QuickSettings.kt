package com.tymwitko.recents.recentapps.quicksettings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.core.net.toUri
import com.tymwitko.recents.R
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.settings.whitelist.WhitelistSettingsData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun QuickSettings(
  app: App,
  posX: Int?,
  posY: Int?,
  settings: StateFlow<WhitelistSettingsData>?,
  hasPrivileges: Boolean,
  fontSize: TextUnit,
  whitelistAppLaunch: (App, Boolean) -> Unit,
  whitelistAppKill: (App, Boolean) -> Unit,
  whitelistAppShow: (App, Boolean) -> Unit,
  onDismissRequest: () -> Unit,
  launchSplitScreen: (App) -> Unit,
  launchFreeForm: (App) -> Unit
) {
  val context = LocalContext.current
  
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
            .padding(vertical = 6.dp)
            .padding(12.dp),
          text = app.name,
          color = MaterialTheme.colorScheme.onBackground,
          style = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = fontSize
          )
        )

        QuickSettingsItem(
          modifier = Modifier.fillMaxWidth(),
          text = stringResource(R.string.app_info),
          settings = MutableStateFlow(null),
          settingType = null,
          fontSize = fontSize,
          onCheck = {
            val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            i.addCategory(Intent.CATEGORY_DEFAULT)
            i.data = ("package:${app.packageName}").toUri()
            context.startActivity(i)
            onDismissRequest()
          }
        )
        QuickSettingsItem(
          modifier = Modifier.fillMaxWidth(),
          text = stringResource(R.string.uninstall_app),
          settings = MutableStateFlow(null),
          settingType = null,
          fontSize = fontSize,
          onCheck = {
            val i = Intent(Intent.ACTION_DELETE)
            i.data = ("package:${app.packageName}").toUri()
            context.startActivity(i)
            onDismissRequest()
          }
        )

        QuickSettingsItem(
          modifier = Modifier.fillMaxWidth(),
          text = stringResource(R.string.split_screen),
          settings = MutableStateFlow(null),
          settingType = null,
          fontSize = fontSize,
          onCheck = {
            launchSplitScreen(app)
            onDismissRequest()
          }
        )
        QuickSettingsItem(
          modifier = Modifier.fillMaxWidth(),
          text = stringResource(R.string.freeform),
          settings = MutableStateFlow(null),
          settingType = null,
          fontSize = fontSize,
          onCheck = {
            launchFreeForm(app)
            onDismissRequest()
          }
        )
        settings?.let { sets ->
          QuickSettingsItem(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.launch),
            settings = sets,
            settingType = WhitelistSettingType.LAUNCH,
            fontSize = fontSize,
            onCheck = {
              whitelistAppLaunch(app, it)
            }
          )
          if (hasPrivileges)
            QuickSettingsItem(
              modifier = Modifier.fillMaxWidth(),
              text = stringResource(R.string.kill),
              settings = sets,
              settingType = WhitelistSettingType.KILL,
              fontSize = fontSize,
              onCheck = {
                whitelistAppKill(app, it)
              }
            )
          QuickSettingsItem(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.show),
            settings = sets,
            settingType = WhitelistSettingType.SHOW,
            fontSize = fontSize,
            onCheck = {
              whitelistAppShow(app, it)
            }
          )
        }
      }
    }
  }
}
