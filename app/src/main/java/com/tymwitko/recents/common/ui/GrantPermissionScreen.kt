package com.tymwitko.recents.common.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tymwitko.recents.R

@Composable
fun GrantPermissionScreen(content: @Composable () -> Unit = {}) {
  Column(
    modifier = Modifier
      .statusBarsPadding()
      .navigationBarsPadding(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      modifier = Modifier.padding(16.dp),
      text = stringResource(R.string.usage_stats_manual),
      color = MaterialTheme.colorScheme.onBackground
    )
    content()
  }
}
