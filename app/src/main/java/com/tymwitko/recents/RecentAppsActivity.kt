package com.tymwitko.recents

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.tymwitko.recents.dataclasses.App
import com.tymwitko.recents.exceptions.AppNotKilledException
import com.tymwitko.recents.ui.compost.RecentAppsItem
import com.tymwitko.recents.ui.compost.RecentAppsTheme
import com.tymwitko.recents.viewmodels.RecentAppsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class RecentAppsActivity : AppCompatActivity() {

    private val viewModel by viewModel<RecentAppsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RecentAppsTheme {
                val modifierForBars =
                    if (viewModel.hasRoot()) Modifier.statusBarsPadding().navigationBarsPadding()
                    else Modifier.statusBarsPadding()
                Column(
                    modifier = modifierForBars,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RecentAppsList(
                        modifier = Modifier.fillMaxHeight().weight(1f),
                        appList = viewModel.getActiveApps(
                            packageName,
                            ResourcesCompat.getDrawable(
                                resources,
                                android.R.drawable.ic_menu_gallery,
                                null
                            )
                                ?.toBitmap()?.asImageBitmap()
                        ),
                        launchApp = ::launchApp,
                        killApp = ::killByPackageName,
                        hasRoot = viewModel.hasRoot()
                    )
                    if (viewModel.hasRoot()) {
                        Button(modifier = Modifier.padding(16.dp), onClick = ::killAll) {
                            Text(text = resources.getString(R.string.kill_all_apps))
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
                resources.getString(R.string.failed_to_kill_all),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun killByPackageName(packageName: String) {
        val packageInfo = packageManager?.getPackageInfo(packageName, 0)
        packageInfo?.let {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    viewModel.killByPackageInfo(it)
                    Log.d("TAG", "Killed $packageName")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            baseContext,
                            resources.getString(R.string.killed_app, packageName),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: AppNotKilledException) {
                    Log.d("TAG", "Failed to kill $packageName")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            baseContext,
                            resources.getString(R.string.failed_to_kill_app, packageName),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    fun launchApp(packageName: String) {
        if (!viewModel.launchApp(packageName, ::startActivity))
            Toast.makeText(this, R.string.failed_to_launch, Toast.LENGTH_LONG).show()
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
