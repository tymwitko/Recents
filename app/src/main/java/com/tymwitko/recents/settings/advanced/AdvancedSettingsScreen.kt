package com.tymwitko.recents.settings.advanced

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.navigation.NavHostController
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
      .padding(vertical = 24.dp, horizontal = 8.dp)
  ) {
    Row {
      Text(
        text = "Show only running apps",
        color = MaterialTheme.colorScheme.onBackground
      )
      Checkbox(
        checked = checked,
        onCheckedChange = { isChecked ->
          checked = isChecked
          viewModel.saveOnlyRunning(isChecked)
        }
      )
    }
  }
}
