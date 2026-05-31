package com.tymwitko.recents.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OverlayRefreshPresetSelector(
    selectedSeconds: Int,
    onSecondsSelected: (Int) -> Unit,
) {
    val presets = listOf(3, 5, 10, 30)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Overlay refresh: ${selectedSeconds}s",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                space = 8.dp,
                alignment = Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            presets.forEach { seconds ->
                if (seconds == selectedSeconds) {
                    Button(
                        onClick = {
                            onSecondsSelected(seconds)
                        }
                    ) {
                        Text("${seconds}s")
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            onSecondsSelected(seconds)
                        }
                    ) {
                        Text("${seconds}s")
                    }
                }
            }
        }
    }
}