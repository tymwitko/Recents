package com.tymwitko.recents.settings.whitelist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import com.tymwitko.recents.R
import com.tymwitko.recents.common.ui.clearFocusOnKeyboardDismiss
import com.tymwitko.recents.settings.menu.WhitelistAppList
import com.tymwitko.recents.settings.navi.NavigationItem
import com.tymwitko.recents.settings.whitelist.ui.WhitelistItemData
import org.koin.androidx.compose.koinViewModel

@Composable
fun WhitelistSettingsScreen(
  viewModel: WhitelistViewModel = koinViewModel(),
  thisPackageName: String,
  lifecycleOwner: LifecycleOwner,
  defaultIcon: ImageBitmap?,
  navController: NavHostController
) {
  BackHandler {
    navController.navigate(NavigationItem.Menu.route)
  }
  Column(
    modifier = Modifier.statusBarsPadding().navigationBarsPadding(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    val appList = viewModel.getAllPackages(
      thisPackageName,
      defaultIcon
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
            WhitelistItemData(it, viewModel.getSettingsForApp(it.packageName))
          },
        fontSize = viewModel.getFontSize(),
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
        lifecycleOwner = lifecycleOwner,
        iconSize =
          viewModel.getIconSize(dimensionResource(R.dimen.icon_dimension).value.toInt())
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
