package com.tymwitko.recents.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SizeSlider(
  sliderPosition: Float,
  onChange: (Float) -> Unit
) {
  Column(
    modifier = Modifier.padding(24.dp)
  ) {
    Slider(
      value = sliderPosition,
      onValueChange = onChange,
      steps = 20,
      valueRange = 3F..24F
    )
    Text(text = sliderPosition.toString(), color = MaterialTheme.colorScheme.onBackground)
  }
}
