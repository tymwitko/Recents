package com.tymwitko.recents.recentapps

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.MutableLiveData
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
import com.tymwitko.recents.common.ui.compost.RecentAppsTheme
import com.tymwitko.recents.common.ui.toImageBitmap
import com.tymwitko.recents.overlay.OverlayActivity
import com.tymwitko.recents.overlay.OverlayModePrefs
import com.tymwitko.recents.overlay.OverlayRefreshPrefs
import com.tymwitko.recents.overlay.OverlayRefreshPresetSelector
import com.tymwitko.recents.overlay.OverlayService
import com.tymwitko.recents.overlay.RecentAppsCache
import com.tymwitko.recents.recentapps.quicksettings.QuickSettingsItem
import com.tymwitko.recents.recentapps.quicksettings.WhitelistSettingType
import com.tymwitko.recents.settings.SettingsActivity
import com.tymwitko.recents.settings.whitelist.WhitelistSettingsData
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
  }

  private fun onRequestPermissionsResult(grantResult: Int) {
    val granted = grantResult == PackageManager.PERMISSION_GRANTED
    if (granted) {
      updateList()
    }
  }

  private fun setupViews(): Unit = setContent {
    RecentAppsTheme {
      val appList: List<App>? by viewModel.appList.collectAsStateWithLifecycle(null)

      var overlayModeEnabled by remember {
        mutableStateOf(OverlayModePrefs.isEnabled(this@RecentAppsActivity))
      }

      var overlayRefreshSeconds by remember {
        mutableIntStateOf(
          OverlayRefreshPrefs.getRefreshSeconds(
            context = this@RecentAppsActivity
          )
        )
      }

      var showSettingsForPackage: Pair<String, String>? by remember {
        mutableStateOf(null)
      }

      var longPressX: Int? by remember {
        mutableStateOf(null)
      }

      var longPressY: Int? by remember {
        mutableStateOf(null)
      }

      val haptics = LocalHapticFeedback.current
      val context = LocalContext.current

      val imageLoader = ImageLoader.Builder(context)
        .components {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            add(ImageDecoderDecoder.Factory())
          } else {
            add(GifDecoder.Factory())
          }
        }
        .build()

      LaunchedEffect(RECENTS_EFFECT_KEY) {
        updateList()
        viewModel.requestShizuku()
      }

      LaunchedEffect(appList) {
        appList?.let { apps ->
          RecentAppsCache.update(apps)
        }
      }

      when {
        appList == null -> {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            Image(
              painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(context)
                  .data(data = R.drawable.loading)
                  .apply {
                    size(Size.ORIGINAL)
                  }
                  .build(),
                imageLoader = imageLoader
              ),
              contentDescription = null
            )
          }
        }

        appList?.isNotEmpty() == true -> {
          viewModel.shutdownShizuku()
          viewModel.hideSystemApps(appList!!)

          Column(
            modifier = Modifier
              .statusBarsPadding()
              .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .weight(1f)
            ) {
              val appWidgetLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
              ) {
                // Nothing needed here.
              }

              RecentAppsList(
                modifier = Modifier.fillMaxHeight(),
                appList = appList!!,
                launchApp = { app: App ->
                  launchApp(app, appWidgetLauncher::launch)
                },
                showQuickSettings = { pkg: String, name: String, x: Int, y: Int ->
                  showSettingsForPackage = pkg to name
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
                  ResourcesCompat.getDrawable(resources, R.drawable.settings, theme)
                    ?.toBitmap()
                    ?.asImageBitmap()
                    ?.let { icon ->
                      Icon(icon, null)
                    }
                }
              )

              showSettingsForPackage?.let {
                QuickSettings(
                  packageName = it.first,
                  appName = it.second,
                  posX = longPressX,
                  posY = longPressY,
                  settings = viewModel.getSettingsForApp(it.first)
                ) {
                  showSettingsForPackage = null
                }
              }
            }

            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Button(
                  onClick = {
                    overlayModeEnabled = !overlayModeEnabled
                    OverlayModePrefs.setEnabled(this@RecentAppsActivity, overlayModeEnabled)

                    if (overlayModeEnabled) {
                      RecentAppsCache.update(appList.orEmpty())
                      startActivity(Intent(this@RecentAppsActivity, OverlayActivity::class.java))
                      finish()
                    } else {
                      OverlayService.stop(this@RecentAppsActivity)
                    }
                  }
                ) {
                  Text(
                    text = if (overlayModeEnabled) {
                      "DISABLE OVERLAY MODE"
                    } else {
                      "ENABLE OVERLAY MODE"
                    }
                  )
                }

                if (viewModel.hasPrivileges()) {
                  Button(
                    modifier = Modifier.padding(start = 12.dp),
                    onClick = ::killAll
                  ) {
                    Text(text = stringResource(R.string.kill_all_apps))
                  }
                }
              }

              OverlayRefreshPresetSelector(
                selectedSeconds = overlayRefreshSeconds,
                onSecondsSelected = { seconds ->
                  overlayRefreshSeconds = seconds

                  OverlayRefreshPrefs.setRefreshSeconds(
                    context = this@RecentAppsActivity,
                    seconds = seconds
                  )

                  OverlayService.refresh(this@RecentAppsActivity)
                }
              )
            }
          }
        }

        viewModel.isOnlyRunning() -> {
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

            Button(
              modifier = Modifier.padding(top = 16.dp),
              onClick = {
                overlayModeEnabled = !overlayModeEnabled
                OverlayModePrefs.setEnabled(this@RecentAppsActivity, overlayModeEnabled)

                if (overlayModeEnabled) {
                  startActivity(Intent(this@RecentAppsActivity, OverlayActivity::class.java))
                  finish()
                } else {
                  OverlayService.stop(this@RecentAppsActivity)
                }
              }
            ) {
              Text(
                text = if (overlayModeEnabled) {
                  "DISABLE OVERLAY MODE"
                } else {
                  "ENABLE OVERLAY MODE"
                }
              )
            }
          }
        }

        else -> {
          viewModel.requestShizuku()

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

            Button(
              modifier = Modifier.padding(16.dp),
              onClick = {
                updateList()
              }
            ) {
              Text(
                modifier = Modifier.padding(16.dp),
                text = stringResource(R.string.done)
              )
            }

            Button(
              modifier = Modifier.padding(16.dp),
              onClick = {
                overlayModeEnabled = !overlayModeEnabled
                OverlayModePrefs.setEnabled(this@RecentAppsActivity, overlayModeEnabled)

                if (overlayModeEnabled) {
                  startActivity(Intent(this@RecentAppsActivity, OverlayActivity::class.java))
                  finish()
                } else {
                  OverlayService.stop(this@RecentAppsActivity)
                }
              }
            ) {
              Text(
                modifier = Modifier.padding(16.dp),
                text = if (overlayModeEnabled) {
                  "DISABLE OVERLAY MODE"
                } else {
                  "ENABLE OVERLAY MODE"
                }
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
        this,
        R.string.failed_to_kill_all,
        Toast.LENGTH_SHORT
      ).show()
    }
  }

  @Composable
  fun QuickSettings(
    packageName: String,
    appName: String,
    posX: Int?,
    posY: Int?,
    settings: MutableLiveData<WhitelistSettingsData>?,
    onDismissRequest: () -> Unit
  ) {
    Popup(
      popupPositionProvider = object : PopupPositionProvider {
        override fun calculatePosition(
          anchorBounds: IntRect,
          windowSize: IntSize,
          layoutDirection: LayoutDirection,
          popupContentSize: IntSize
        ) = IntOffset(
          (posX?.minus(popupContentSize.width / 2)) ?: 0,
          (posY?.minus(popupContentSize.height)) ?: 0
        )
      },
      onDismissRequest = onDismissRequest,
      properties = PopupProperties(focusable = true)
    ) {
      Surface(
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(
          modifier = Modifier
            .padding(0.dp)
            .width(IntrinsicSize.Max)
            .border(
              width = 1.dp,
              color = Color.DarkGray,
              shape = RoundedCornerShape(12.dp)
            )
        ) {
          Text(
            modifier = Modifier
              .fillMaxWidth()
              .border(
                width = 1.dp,
                color = Color.DarkGray,
                shape = CutCornerShape(0.dp)
              )
              .padding(12.dp),
            text = appName
          )

          QuickSettingsItem(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.launch),
            settings = settings,
            lifecycleOwner = this@RecentAppsActivity,
            settingType = WhitelistSettingType.LAUNCH,
            onCheck = {
              viewModel.whitelistAppLaunch(packageName, it)
            }
          )

          if (viewModel.hasPrivileges()) {
            QuickSettingsItem(
              modifier = Modifier.fillMaxWidth(),
              text = stringResource(R.string.kill),
              settings = settings,
              lifecycleOwner = this@RecentAppsActivity,
              settingType = WhitelistSettingType.KILL,
              onCheck = {
                viewModel.whitelistAppKill(packageName, it)
              }
            )
          }

          QuickSettingsItem(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.show),
            settings = settings,
            lifecycleOwner = this@RecentAppsActivity,
            settingType = WhitelistSettingType.SHOW,
            onCheck = {
              viewModel.whitelistAppShow(packageName, it)
            }
          )
        }
      }
    }
  }

  private fun launchApp(app: App, startActivity: (Intent) -> Unit) {
    if (!viewModel.launchApp(app, startActivity)) {
      Toast.makeText(this, R.string.failed_to_launch, Toast.LENGTH_LONG).show()
      throw AppNotLaunchedException()
    }
  }

  private fun updateList() {
    viewModel.fetchApps(packageName)
  }
}

@Composable
fun RecentAppsList(
  modifier: Modifier = Modifier,
  appList: List<App>,
  launchApp: (App) -> Unit,
  showQuickSettings: (String, String, Int, Int) -> Unit
) {
  LazyColumn(modifier = modifier) {
    items(
      items = appList,
      key = { app: App -> app.packageName }
    ) { app: App ->
      RecentAppsItem(
        app = app,
        launchApp = launchApp,
        showQuickSettings = showQuickSettings
      )
    }
  }
}