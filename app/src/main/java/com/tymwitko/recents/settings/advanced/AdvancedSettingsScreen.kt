package com.tymwitko.recents.settings.advanced

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.tymwitko.recents.R
import com.tymwitko.recents.settings.navi.NavigationItem
import org.koin.androidx.compose.koinViewModel

@Composable
fun AdvancedSettingsScreen(
  navController: NavHostController,
  viewModel: AdvancedSettingsViewModel = koinViewModel(),
) {
  BackHandler {
    navController.navigate(NavigationItem.Menu.route)
  }
  var checked by rememberSaveable { mutableStateOf(viewModel.getOnlyRunning()) }
  Column(
    modifier = Modifier
      .statusBarsPadding()
      .navigationBarsPadding()
      .fillMaxSize()
  ) {
    AdvancedSettingsItem(
      title = stringResource(R.string.only_running),
      note = stringResource(R.string.running_apps_note)
    ) {
      Switch(
        checked = checked,
        enabled = viewModel.canSetOnlyRunning(),
        onCheckedChange = { isChecked ->
          checked = isChecked
          viewModel.saveOnlyRunning(isChecked)
        }
      )
    }
    
    AdvancedSettingsItem(
      title = stringResource(R.string.kill_method),
      note = stringResource(R.string.kill_method_note)
    ) {
      var isSwipeSelected by rememberSaveable { mutableStateOf(viewModel.isSwipeToDelete()) }
      SingleChoiceSegmentedButtonRow {
        listOf(
          true, false
        ).forEachIndexed { index, isSwipe ->
          SegmentedButton(
            shape = SegmentedButtonDefaults.itemShape(
              index = index,
              count = 2
            ),
            onClick = {
              isSwipeSelected = isSwipe
              viewModel.saveSwipeToDelete(isSwipe)
            },
            selected = isSwipeSelected == isSwipe,
            label = {
              Text(
                text = stringResource(viewModel.getResourceStringForOption(isSwipe)),
                color = MaterialTheme.colorScheme.onBackground
              )
            }
          )
        }
      }
    }
  }
}
