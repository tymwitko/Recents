package com.tymwitko.recents.recentapps

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.MutableLiveData
import com.tymwitko.recents.R
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.common.ui.compost.RecentAppsTheme
import com.tymwitko.recents.recentapps.quicksettings.QuickSettingsItem
import com.tymwitko.recents.recentapps.quicksettings.WhitelistSettingType
import com.tymwitko.recents.settings.SettingsActivity
import com.tymwitko.recents.settings.whitelist.WhitelistSettingsData
import org.koin.androidx.viewmodel.ext.android.viewModel

class RecentAppsActivity : AppCompatActivity() {

  private val viewModel by viewModel<RecentAppsViewModel>()
  private val placeholderIcon by lazy {
    ResourcesCompat.getDrawable(
      resources,
      android.R.drawable.ic_menu_gallery,
      null
    )?.toBitmap()?.asImageBitmap()
  }
  private val allApps by lazy { 
    viewModel.getActiveApps(packageName, placeholderIcon)
  }
  private var firstRun = true

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
      Column(
        modifier = Modifier
          .statusBarsPadding()
          .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        var appList: List<App> by remember { mutableStateOf(allApps) }
        var showSettingsForPackage: Pair<String, String>? by remember { mutableStateOf(null) }
        var longPressX: Int? by remember { mutableStateOf(null) }
        var longPressY: Int? by remember { mutableStateOf(null) }
        val haptics = LocalHapticFeedback.current
        if (firstRun) {
          updateList()
          Log.d("TAG", "setting listener")
          viewModel.appList.observe(this@RecentAppsActivity) { list ->
            Log.d("TAG", "listener triggered")
            appList = list
          }
          firstRun = false
        }
        if (appList.isNotEmpty()) {
          viewModel.shutdownShizuku()
          viewModel.hideSystemApps(appList)
          Box(modifier = Modifier
            .fillMaxSize()
            .weight(1f)) {
            RecentAppsList(
              modifier = Modifier
                .fillMaxHeight(),
              appList = appList,
              launchApp = ::launchApp,
              killApp = ::killByPackageName,
              showQuickSettings = { pkg, name, x, y ->
                showSettingsForPackage = (pkg to name)
                longPressX = x
                longPressY = y
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              },
              hasPrivileges = viewModel.hasPrivileges(),
              fontSize = viewModel.getFontSize(),
              iconSize = viewModel.getIconSize(dimensionResource(R.dimen.icon_dimension).value.toInt())
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
                  ?.toBitmap()?.asImageBitmap()?.let { Icon(it, null) }
              }
            )
            showSettingsForPackage?.let {
              QuickSettings(
                it.first, 
                it.second,
                longPressX,
                longPressY,
                viewModel.getSettingsForApp(it.first)
              ) {
                showSettingsForPackage = null
              }
            }
          }
          if (viewModel.hasPrivileges()) {
            Button(modifier = Modifier.padding(16.dp), onClick = ::killAll) {
              Text(text = stringResource(R.string.kill_all_apps))
            }
          }
        } else {
          viewModel.requestShizuku()
          Text(
            modifier = Modifier.padding(16.dp),
            text = stringResource(R.string.usage_stats_manual),
            color = MaterialTheme.colorScheme.onBackground
          )
          Button(modifier = Modifier.padding(16.dp), onClick = {
            updateList()
            setupViews()
          }
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

  fun killAll() {
    viewModel.killEmAll(packageName) {
      Toast.makeText(
        this, R.string.failed_to_kill_all, Toast.LENGTH_SHORT
      ).show()
    }
  }

  fun killByPackageName(packageName: String) {
    val packageInfo = packageManager?.getPackageInfo(packageName, 0)
    packageInfo?.let {
      viewModel.killByPackageInfo(
        it,
        onSucc = {
          Log.d("TAG", "Killed $packageName")
          Toast.makeText(
            baseContext,
            resources.getString(R.string.killed_app, packageName),
            Toast.LENGTH_SHORT
          ).show()
        },
        onError =  {
          Log.d("TAG", "Failed to kill $packageName")
          Toast.makeText(
            baseContext,
            resources.getString(R.string.failed_to_kill_app, packageName),
            Toast.LENGTH_SHORT
          ).show()
        }
      )
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
        Column(modifier = Modifier.padding(0.dp).width(IntrinsicSize.Max)
          .border(width = 1.dp, color = Color.DarkGray, shape = RoundedCornerShape(12.dp))
        ) {
          Text(
            modifier = Modifier
              .fillMaxWidth()
              .border(width = 1.dp, color = Color.DarkGray, shape = CutCornerShape(0.dp))
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
          if (viewModel.hasPrivileges())
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

  private fun launchApp(packageName: String) {
    if (!viewModel.launchApp(packageName, ::startActivity))
      Toast.makeText(this, R.string.failed_to_launch, Toast.LENGTH_LONG).show()
  }
  
  private fun updateList() {
    viewModel.getActiveAppsFiltered(
      packageName,
      placeholderIcon
    )
  }
}

@Composable
fun RecentAppsList(
  modifier: Modifier = Modifier,
  appList: List<App>,
  fontSize: TextUnit,
  iconSize: Dp,
  launchApp: (String) -> Unit,
  killApp: (String) -> Unit,
  showQuickSettings: (String, String, Int, Int) -> Unit,
  hasPrivileges: Boolean
) {
  LazyColumn(modifier = modifier) {
    items(items = appList) {
      RecentAppsItem(
        it.name,
        it.packageName,
        it.icon,
        fontSize,
        iconSize,
        launchApp,
        killApp,
        showQuickSettings,
        hasPrivileges
      )
    }
  }
}
