package com.tymwitko.recents.whitelist.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LifecycleOwner
import com.tymwitko.recents.R
import com.tymwitko.recents.common.ui.clearFocusOnKeyboardDismiss
import com.tymwitko.recents.common.ui.compost.RecentAppsTheme
import com.tymwitko.recents.whitelist.WhitelistViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class WhitelistActivity: AppCompatActivity() {
  private val viewModel by viewModel<WhitelistViewModel>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      RecentAppsTheme {
        Column(
          modifier = Modifier.statusBarsPadding().navigationBarsPadding(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          val appList = viewModel.getAllPackages(
            packageName,
            ResourcesCompat.getDrawable(
              resources,
              android.R.drawable.ic_menu_gallery,
              null
            )
              ?.toBitmap()?.asImageBitmap()
          )
          if (appList.isNotEmpty()) {
            val fieldState: TextFieldState = rememberTextFieldState()
            TextField(
              modifier = Modifier.fillMaxWidth().padding(12.dp).clearFocusOnKeyboardDismiss(),
              state = fieldState,
              placeholder = { Text("Search apps") }
            )
            WhitelistAppList(
              modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
              appList = appList
                .filter {
                  it.name.lowercase().contains(fieldState.text.toString().lowercase())
                    || it.packageName.contains(fieldState.text.toString().lowercase())
                }.map {
                  SettingItem(it, viewModel.getSettingsForApp(it.packageName))
                },
              whitelistLaunch = { pack, isChecked ->
                viewModel.whitelistAppLaunch(pack, isChecked)
              },
              whitelistKill = { pack, isChecked ->
                viewModel.whitelistAppKill(pack, isChecked)
              },
              whitelistShow = { pack, isChecked ->
                viewModel.whitelistAppShow(pack, isChecked)
              },
              showKillCheck = viewModel.hasPrivileges(),
              lifecycleOwner = this@WhitelistActivity
            )
          } else {
            Text(
              modifier = Modifier.padding(16.dp),
              text = stringResource(R.string.usage_stats_manual),
              color = MaterialTheme.colorScheme.onBackground
            )
          }
        }
      }
    }
  }
}

@Composable
fun WhitelistAppList(
  modifier: Modifier = Modifier,
  appList: List<SettingItem>,
  whitelistLaunch: (String, Boolean) -> Unit,
  whitelistKill: (String, Boolean) -> Unit,
  whitelistShow: (String, Boolean) -> Unit,
  lifecycleOwner: LifecycleOwner,
  showKillCheck: Boolean
) {
  LazyColumn(modifier = modifier) {
    items(items = appList) {
      WhitelistItem(
        it.app.name,
        it.app.packageName,
        it.app.icon,
        showKillCheck,
        whitelistLaunch,
        whitelistKill,
        whitelistShow,
        it.settings,
        lifecycleOwner
      )
    }
  }
}
