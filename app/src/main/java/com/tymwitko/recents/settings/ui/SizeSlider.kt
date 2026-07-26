package com.tymwitko.recents.settings.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SizeSlider(
  sliderPosition: Float,
  label: String,
  valueRange: ClosedFloatingPointRange<Float>,
  onChange: (Float) -> Unit
) {
  Column(
    modifier = Modifier
      .padding(4.dp)
      .border(width = 1.dp, color = Color.DarkGray, shape = RoundedCornerShape(12.dp))
      .padding(16.dp)
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
