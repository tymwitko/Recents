package com.tymwitko.recents.recentapps

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.tymwitko.recents.R
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.common.ui.compost.RecentAppsTheme
import com.tymwitko.recents.whitelist.ui.WhitelistActivity
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
    setupViews()
  }

  override fun onResume() {
    super.onResume()
    updateList()
    setupViews()
  }

  private fun setupViews(): Unit = setContent {
    RecentAppsTheme {
      Column(
        modifier = Modifier.statusBarsPadding().navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        var appList: List<App> by remember { mutableStateOf(allApps) }
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
          Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            RecentAppsList(
              modifier = Modifier
                .fillMaxHeight(),
              appList = appList,
              launchApp = ::launchApp,
              killApp = ::killByPackageName,
              hasRoot = viewModel.hasRoot()
            )
            FloatingActionButton(
              modifier = Modifier.padding(10.dp).navigationBarsPadding().align(Alignment.BottomEnd),
              onClick = {
                startActivity(
                  Intent(this@RecentAppsActivity, WhitelistActivity::class.java)
                )
              },
              content = {
                ResourcesCompat.getDrawable(resources, R.drawable.settings, theme)
                  ?.toBitmap()?.asImageBitmap()?.let { Icon(it, null) }
              }
            )
          }
          if (viewModel.hasRoot()) {
            Button(modifier = Modifier.padding(16.dp), onClick = ::killAll) {
              Text(text = resources.getString(R.string.kill_all_apps))
            }
          }
        } else {
          Text(
            modifier = Modifier.padding(16.dp),
            text = resources.getString(R.string.usage_stats_manual),
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
        this,
        resources.getString(R.string.failed_to_kill_all),
        Toast.LENGTH_SHORT
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
  launchApp: (String) -> Unit,
  killApp: (String) -> Unit,
  hasRoot: Boolean
) {
  LazyColumn(modifier = modifier) {
    items(items = appList) {
      RecentAppsItem(it.name, it.packageName, it.icon, launchApp, killApp, hasRoot)
    }
  }
}
