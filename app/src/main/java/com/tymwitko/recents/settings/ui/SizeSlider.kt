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
  label: String,
  valueRange: ClosedFloatingPointRange<Float>,
  onChange: (Float) -> Unit
) {
  Column(
    modifier = Modifier.padding(horizontal = 24.dp)
  ) {
    Text(
      text = label,
      color = MaterialTheme.colorScheme.onBackground
    )
    Slider(
      value = sliderPosition,
      onValueChange = onChange,
      steps = 20,
      valueRange = valueRange
    )
  }
}
