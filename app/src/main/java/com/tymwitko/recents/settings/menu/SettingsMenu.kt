package com.tymwitko.recents.settings.menu

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.tymwitko.recents.settings.whitelist.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsMenu(
  modifier: Modifier = Modifier,
  navController: NavHostController,
  entryNames: List<SettingsMenuViewData>,
  viewModel: SettingsViewModel = koinViewModel(),
  exitSettings: () -> Unit
) {
  BackHandler { 
    exitSettings()
  }
  Column(
    modifier = modifier
      .statusBarsPadding()
      .navigationBarsPadding(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
      Box(modifier = Modifier
        .fillMaxSize()
        .weight(1f)) {
        SettingsList(
          modifier = Modifier
            .fillMaxHeight(),
          navController = navController,
          entryNames = entryNames,
          fontSize = viewModel.getFontSize()
        )
      }
    }
  }
