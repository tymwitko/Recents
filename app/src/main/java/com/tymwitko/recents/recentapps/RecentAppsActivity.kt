package com.tymwitko.recents.recentapps

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.tymwitko.recents.R
import com.tymwitko.recents.common.RECENTS_EFFECT_KEY
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.common.exceptions.AppNotLaunchedException
import com.tymwitko.recents.common.ui.GrantPermissionScreen
import com.tymwitko.recents.common.ui.compost.RecentAppsTheme
import com.tymwitko.recents.common.ui.toImageBitmap
import com.tymwitko.recents.recentapps.pinned.ui.PinnedAppPanel
import com.tymwitko.recents.recentapps.quicksettings.QuickSettings
import com.tymwitko.recents.settings.SettingsActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class RecentAppsActivity : AppCompatActivity() {

  private val viewModel by viewModel<RecentAppsViewModel>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    viewModel.setupShizuku(packageName) { _, result ->
      onRequestPermissionsResult(result)
    }
    setupViews()
  }

  override fun onResume() {
    super.onResume()
    updateList()
    setupViews()
  }

  private fun onRequestPermissionsResult(grantResult: Int) {
    val granted = grantResult == PackageManager.PERMISSION_GRANTED
    if (granted) updateList()
  }

  private fun setupViews(): Unit = setContent {
    RecentAppsTheme {
      val appList: List<App>? by viewModel.appList.collectAsStateWithLifecycle(null)
      val pinnedApps: List<App>? by viewModel.pinnedApps.collectAsStateWithLifecycle(null)
      var appWithSettingsShown: App? by remember { mutableStateOf(null) }
      var longPressX: Int? by remember { mutableStateOf(null) }
      var longPressY: Int? by remember { mutableStateOf(null) }
      val haptics = LocalHapticFeedback.current
      val context = LocalContext.current
      val imageLoader = ImageLoader.Builder(context)
        .components {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) add(ImageDecoderDecoder.Factory())
          else add(GifDecoder.Factory())
        }
        .build()
      val hasPrivileges by viewModel.hasPrivileges.collectAsStateWithLifecycle()
      val isSwipeToKill by rememberSaveable { mutableStateOf(viewModel.isSwipeToKill()) }
      val isOnlyRunning by rememberSaveable { mutableStateOf(viewModel.isOnlyRunning()) }
      LaunchedEffect(RECENTS_EFFECT_KEY) {
        updateList()
        viewModel.checkPrivileges()
        viewModel.requestShizuku()
      }
      when {
        appList == null -> {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            Image(
              painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(context).data(data = R.drawable.loading)
                  .apply(
                    block = {
                      size(Size.ORIGINAL)
                    }
                  ).build(),
                imageLoader = imageLoader
              ),
              contentDescription = null
            )
          }
        }

        appList?.isNotEmpty() == true -> {
          viewModel.shutdownShizukuPermissionListener()
          Column(
            modifier = Modifier
              .statusBarsPadding()
              .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            pinnedApps?.takeIf { it.isNotEmpty() }?.let {
              PinnedAppPanel(
                apps = it,
                iconSize = viewModel.getIconSize(
                  dimensionResource(R.dimen.icon_dimension).value.toInt()
                ),
                launchApp = { p ->
                  launchApp(p, ::startActivity)
                },
              )
            }
            Box(
              modifier = Modifier
                .fillMaxSize()
                .weight(1f)
            ) {
              RecentAppsList(
                modifier = Modifier
                  .fillMaxHeight(),
                appList = appList!!,
                hasPrivileges = hasPrivileges,
                isSwipeToKill = isSwipeToKill,
                launchApp = { p ->
                  launchApp(p, ::startActivity)
                },
                iconSize = viewModel.getIconSize(
                  dimensionResource(R.dimen.icon_dimension).value.toInt()
                ),
                fontSize = viewModel.getFontSize(),
                showQuickSettings = { app, x, y ->
                  appWithSettingsShown = app
                  longPressX = x
                  longPressY = y
                  haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                }
              )
              FloatingActionButton(
                modifier = Modifier
                  .padding(10.dp)
                  .navigationBarsPadding()
                  .align(Alignment.BottomEnd),
                onClick = {
                  startActivity(
                    Intent(this@RecentAppsActivity, SettingsActivity::class.java)
                  )
                },
                content = {
                  painterResource(R.drawable.settings)
                    .toImageBitmap(LocalDensity.current, LocalLayoutDirection.current)
                    .let { Icon(it, null) }
                }
              )
              appWithSettingsShown?.let {
                QuickSettings(
                  it,
                  longPressX,
                  longPressY,
                  viewModel.getSettingsForApp(it.getId()),
                  hasPrivileges,
                  viewModel.getFontSize(),
                  viewModel::whitelistAppLaunch,
                  viewModel::whitelistAppKill,
                  viewModel::whitelistAppShow,
                  {
                    appWithSettingsShown = null
                  },
                  { app ->
                    val lastApp = when {
                      appList == null || appList!!.size < 2 -> null
                      app.packageName == appList?.first()?.packageName -> appList?.get(1)
                      else -> appList?.firstOrNull()
                    }
                    lastApp?.let { it1 ->
                      viewModel.launchAppsInSplitScreen(app, it1, ::startActivity) {
                        Toast.makeText(
                          context,
                          resources.getString(R.string.split_work_apps),
                          Toast.LENGTH_SHORT
                        ).show()
                      }
                    } ?: run {
                      Toast.makeText(
                        context,
                        resources.getString(R.string.split_needs_2),
                        Toast.LENGTH_SHORT
                      ).show()
                    }
                  },
                  { app ->
                    try {
                      viewModel.launchFreeForm(app, ::startActivity)
                    } catch (e: Exception) {
                      Log.d("TAG", "Launching app in free form mode failed! ${e.stackTrace}")
                      Toast.makeText(
                        context,
                        resources.getString(R.string.freeform_device_unsupported),
                        Toast.LENGTH_SHORT
                      ).show()
                    }
                  }
                )
              }
            }
            if (hasPrivileges) {
              Button(modifier = Modifier.padding(16.dp), onClick = ::killAll) {
                Text(text = stringResource(R.string.kill_all_apps))
              }
            }
          }
        }

        isOnlyRunning -> {
          Box(
            modifier = Modifier.fillMaxSize()
          ) {
            Column(
              modifier = Modifier
                .statusBarsPadding()
                .navigationBarsPadding(),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              pinnedApps?.takeIf { it.isNotEmpty() }?.let {
                PinnedAppPanel(
                  apps = it,
                  iconSize = viewModel.getIconSize(
                    dimensionResource(R.dimen.icon_dimension).value.toInt()
                  ),
                  launchApp = { p ->
                    launchApp(p, ::startActivity)
                  },
                )
              }
              Column(
                modifier = Modifier
                  .statusBarsPadding()
                  .navigationBarsPadding()
                  .fillMaxSize()
                  .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                Image(
                  bitmap = painterResource(R.drawable.error_emoji).toImageBitmap(
                    LocalDensity.current,
                    LocalLayoutDirection.current
                  ),
                  contentDescription = null
                )
                Text(
                  text = stringResource(R.string.running_empty),
                  color = MaterialTheme.colorScheme.onBackground
                )
              }
            }
            FloatingActionButton(
              modifier = Modifier
                .padding(10.dp)
                .navigationBarsPadding()
                .align(Alignment.BottomEnd),
              onClick = {
                startActivity(
                  Intent(this@RecentAppsActivity, SettingsActivity::class.java)
                )
              },
              content = {
                painterResource(R.drawable.settings)
                  .toImageBitmap(LocalDensity.current, LocalLayoutDirection.current)
                  .let { Icon(it, null) }
              }
            )
          }
        }

        else -> {
          viewModel.requestShizuku()
          GrantPermissionScreen {
            Button(
              modifier = Modifier.padding(16.dp),
              onClick = { updateList() }
            ) {
              Text(
                modifier = Modifier.padding(16.dp),
                text = stringResource(R.string.done)
              )
            }
          }
        }
      }
    }
  }

  fun killAll() {
    viewModel.killEmAll(packageName) {
      Toast.makeText(
        this, R.string.failed_to_kill_all, Toast.LENGTH_SHORT
      ).show()
    }
  }

  private fun launchApp(app: App, startActivity: (Intent, Bundle?) -> Unit) {
    if (!viewModel.launchApp(app, startActivity)) {
      Toast.makeText(this, R.string.failed_to_launch, Toast.LENGTH_LONG).show()
      throw AppNotLaunchedException()
    } else {
      app.isRunning = true
      viewModel.updateAppInPinned(app, true)
    }
  }

  private fun updateList() {
    viewModel.fetchApps(
      packageName
    )
  }
}
