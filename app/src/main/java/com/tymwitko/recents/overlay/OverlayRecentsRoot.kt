package com.tymwitko.recents.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tymwitko.recents.common.dataclasses.App
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun OverlayRecentsRoot(
    apps: List<App>,
    isLoading: Boolean,
    status: String,
    hasPrivileges: Boolean,
    canKillApp: (App) -> Boolean,
    canKillAny: Boolean,
    onClose: () -> Unit,
    onOpenFullApp: () -> Unit,
    onRefresh: () -> Unit,
    onLaunchApp: (App) -> Unit,
    onKillApp: (App) -> Unit,
    onKillAll: () -> Unit,
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    onClose()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .pointerInput(Unit) {
                    detectTapGestures {
                        // Consume taps inside the overlay card so only outside taps close it.
                    }
                },
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
            shadowElevation = 14.dp,
            tonalElevation = 6.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OverlayHeader(
                    hasPrivileges = hasPrivileges,
                    canKillAny = canKillAny,
                    onOpenFullApp = onOpenFullApp,
                    onRefresh = onRefresh,
                    onKillAll = onKillAll,
                    onClose = onClose
                )

                Text(
                    text = status,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 14.dp)
                )

                when {
                    isLoading -> LoadingState()
                    apps.isEmpty() -> EmptyState()
                    else -> AppStrip(
                        apps = apps,
                        canKillApp = canKillApp,
                        onLaunchApp = onLaunchApp,
                        onKillApp = onKillApp
                    )
                }

                Text(
                    text = if (hasPrivileges) {
                        "Tap to open  •  Swipe up/down to kill  •  Scroll left/right"
                    } else {
                        "Tap to open  •  Shizuku/root required to kill  •  Scroll left/right"
                    },
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 10.dp),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun OverlayHeader(
    hasPrivileges: Boolean,
    canKillAny: Boolean,
    onOpenFullApp: () -> Unit,
    onRefresh: () -> Unit,
    onKillAll: () -> Unit,
    onClose: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderButton(Icons.Default.Settings, "Open full app", onOpenFullApp)
            HeaderButton(Icons.Default.Refresh, "Refresh apps", onRefresh)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Recent apps",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f)
            )

            Text(
                text = if (hasPrivileges) "kill enabled" else "switch only",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.42f)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (canKillAny) {
                HeaderButton(Icons.Default.Delete, "Kill all apps", onKillAll)
            }

            HeaderButton(Icons.Default.Close, "Close overlay", onClose)
        }
    }
}

@Composable
private fun HeaderButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .pointerInput(Unit) {
                detectTapGestures {
                    onClick()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun LoadingState() {
    CircularProgressIndicator(
        modifier = Modifier
            .padding(vertical = 18.dp)
            .size(28.dp),
        strokeWidth = 2.dp
    )
}

@Composable
private fun EmptyState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp)
    ) {
        Text(
            text = "No recent apps found",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "Open the full app and check Usage Access.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.42f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun AppStrip(
    apps: List<App>,
    canKillApp: (App) -> Boolean,
    onLaunchApp: (App) -> Unit,
    onKillApp: (App) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = apps,
            key = { app -> app.packageName }
        ) { app ->
            SwipeToKillIcon(
                app = app,
                canKill = canKillApp(app),
                onKill = { onKillApp(app) },
                onLaunch = { onLaunchApp(app) }
            )
        }
    }
}

@Composable
private fun SwipeToKillIcon(
    app: App,
    canKill: Boolean,
    onKill: () -> Unit,
    onLaunch: () -> Unit,
) {
    val swipeThreshold = 120f

    var visible by remember { mutableStateOf(true) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val progress = (abs(offsetY) / swipeThreshold).coerceIn(0f, 1f)
    val isPastHalf = progress > 0.5f

    AnimatedVisibility(
        visible = visible,
        exit = fadeOut(tween(160)) + slideOutVertically(tween(160)) { -it }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset { IntOffset(0, offsetY.roundToInt()) }
                .alpha(1f - progress)
                .pointerInput(canKill) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (canKill && abs(offsetY) >= swipeThreshold) {
                                visible = false
                                onKill()
                            } else {
                                offsetY = 0f
                            }
                        },
                        onDragCancel = {
                            offsetY = 0f
                        },
                        onVerticalDrag = { _, delta ->
                            if (canKill) {
                                offsetY += delta
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            onLaunch()
                        }
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isPastHalf && canKill) {
                            Color.Red.copy(alpha = 0.18f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                app.icon?.let { icon ->
                    Image(
                        bitmap = icon,
                        contentDescription = app.name,
                        modifier = Modifier.size(50.dp)
                    )
                } ?: Icon(
                    painter = painterResource(android.R.drawable.sym_def_app_icon),
                    contentDescription = app.name,
                    modifier = Modifier.size(50.dp)
                )

                if (isPastHalf && canKill) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.Red.copy(alpha = 0.38f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kill",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Text(
                text = app.name,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(top = 5.dp)
                    .size(width = 64.dp, height = 16.dp)
            )
        }
    }
}
