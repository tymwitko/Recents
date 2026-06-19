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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.tymwitko.recents.R
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.settings.whitelist.WhitelistSettingsData

@Composable
fun QuickSettings(
  app: App,
  posX: Int?,
  posY: Int?,
  settings: MutableLiveData<WhitelistSettingsData>?,
  hasPrivileges: Boolean,
  lifecycleOwner: LifecycleOwner,
  whitelistAppLaunch: (String, Boolean) -> Unit,
  whitelistAppKill: (String, Boolean) -> Unit,
  whitelistAppShow: (String, Boolean) -> Unit,
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
            .padding(12.dp),
          text = app.name
        )

        QuickSettingsItem(
          modifier = Modifier.fillMaxWidth(),
          text = stringResource(R.string.app_info),
          settings = null,
          lifecycleOwner = lifecycleOwner,
          settingType = null,
          triggerHandler = {
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
          settings = null,
          lifecycleOwner = lifecycleOwner,
          settingType = null,
          triggerHandler = {
            val i = Intent(Intent.ACTION_DELETE)
            i.data = ("package:${app.packageName}").toUri()
            context.startActivity(i)
            onDismissRequest()
          }
        )

        QuickSettingsItem(
          modifier = Modifier.fillMaxWidth(),
          text = stringResource(R.string.split_screen),
          settings = null,
          lifecycleOwner = lifecycleOwner,
          settingType = null,
          triggerHandler = {
            launchSplitScreen(app)
            onDismissRequest()
          }
        )
        QuickSettingsItem(
          modifier = Modifier.fillMaxWidth(),
          text = stringResource(R.string.freeform),
          settings = null,
          lifecycleOwner = lifecycleOwner,
          settingType = null,
          triggerHandler = {
            launchFreeForm(app)
            onDismissRequest()
          }
        )
        QuickSettingsItem(
          modifier = Modifier.fillMaxWidth(),
          text = stringResource(R.string.launch),
          settings = settings,
          lifecycleOwner = lifecycleOwner,
          settingType = WhitelistSettingType.LAUNCH,
          triggerHandler = {
            whitelistAppLaunch(app.packageName, it)
          }
        )
        if (hasPrivileges)
          QuickSettingsItem(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.kill),
            settings = settings,
            lifecycleOwner = lifecycleOwner,
            settingType = WhitelistSettingType.KILL,
            triggerHandler = {
              whitelistAppKill(app.packageName, it)
            }
          )
        QuickSettingsItem(
          modifier = Modifier.fillMaxWidth(),
          text = stringResource(R.string.show),
          settings = settings,
          lifecycleOwner = lifecycleOwner,
          settingType = WhitelistSettingType.SHOW,
          triggerHandler = {
            whitelistAppShow(app.packageName, it)
          }
        )
      }
    }
  }
}
