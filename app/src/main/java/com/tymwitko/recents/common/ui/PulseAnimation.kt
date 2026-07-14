package com.tymwitko.recents.common.ui

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tymwitko.recents.common.ui.compost.RecentAppsTheme

@Composable
fun PulseAnimation(
  modifier: Modifier = Modifier,
  color: Color = MaterialTheme.colorScheme.onBackground
) {
  val infiniteTransition = rememberInfiniteTransition()
  val progress by infiniteTransition.animateFloat(
    initialValue = 0F,
    targetValue = 1F,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2000)
    )
  )
  Box(
    modifier = modifier.size(60.dp)
      .graphicsLayer {
        scaleX = progress
        scaleY = progress
        alpha = 1f - progress
      }
      .background(
        color = color,
        shape = CircleShape
      ),
    contentAlignment = Alignment.Center
  ) {}
}

@Preview
@Composable
fun AnimationPreview() {
  RecentAppsTheme {
    PulseAnimation()
  }
}
