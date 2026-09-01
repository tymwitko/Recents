package com.tymwitko.recents.common.ui

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.tymwitko.recents.R
import com.tymwitko.recents.common.ERROR_ISSUE_URL

@Composable
fun ErrorScreen(
  errorMessage: String,
  refresh: () -> Unit,
  copyMessage: (String) -> Unit
) {
  Column(
    modifier = Modifier
      .statusBarsPadding()
      .navigationBarsPadding(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    val context = LocalContext.current
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
        painter = painterResource(R.drawable.error_emoji),
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
          .fillMaxSize()
          .weight(1f)
      ) {
        Box(
          contentAlignment = Alignment.TopEnd
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
          Button(
            modifier = Modifier.alpha(0.8f).width(80.dp).height(80.dp),
            onClick = {
              copyMessage(errorMessage)
            },
            colors = ButtonColors(
              containerColor = Color.Transparent,
              contentColor = MaterialTheme.colorScheme.onBackground,
              disabledContainerColor = Color.Transparent,
              disabledContentColor = MaterialTheme.colorScheme.onBackground
            ),
            shape = RectangleShape
          ) {
            Image(
              painter = painterResource(R.drawable.copy),
              contentDescription = null
            )
          }
        }
      }
      Button(
        onClick = refresh
      ) {
        Text(
          text = stringResource(R.string.retry)
        )
      }
      Button(
        onClick = {
          copyMessage(errorMessage)
          val browserIntent = Intent(Intent.ACTION_VIEW, ERROR_ISSUE_URL.toUri())
          context.startActivity(browserIntent)
        }
      ) {
        Text(
          text = stringResource(R.string.report_error)
        )
      }
    }
  }
}
