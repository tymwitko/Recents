package com.tymwitko.recents.common.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.tymwitko.recents.R

@Composable
fun ErrorScreen(errorMessage: String) {
  Column(
    modifier = Modifier
      .statusBarsPadding()
      .navigationBarsPadding(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Column(
      modifier = Modifier
        .statusBarsPadding()
        .navigationBarsPadding()
        .fillMaxSize()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
      Image(
        bitmap = painterResource(R.drawable.error_emoji).toImageBitmap(
          LocalDensity.current,
          LocalLayoutDirection.current
        ),
        contentDescription = null
      )
      Text(
        text = stringResource(R.string.error),
        color = MaterialTheme.colorScheme.onBackground
      )
      SelectionContainer(
        modifier = Modifier.background(
          color = MaterialTheme.colorScheme.onError
        )
      ) {
        LazyColumn(
          modifier = Modifier.padding(4.dp)
        ) {
          items(items = listOf(Unit), key = { "" }) {
            Text(
              text = errorMessage,
              color = MaterialTheme.colorScheme.onErrorContainer,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    }
  }
}
