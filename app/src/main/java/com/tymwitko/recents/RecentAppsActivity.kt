package com.tymwitko.recents

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.tymwitko.recents.dataclasses.App
import com.tymwitko.recents.ui.compost.RecentAppsItem
import com.tymwitko.recents.ui.compost.RecentAppsTheme
import com.tymwitko.recents.viewmodels.RecentAppsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class RecentAppsActivity : AppCompatActivity() {

    private val viewModel by viewModel<RecentAppsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RecentAppsTheme {
                RecentAppsList(
                    appList = viewModel.getActiveApps(
                        packageName,
                        ResourcesCompat.getDrawable(resources, android.R.drawable.ic_menu_gallery, null)
                    ?.toBitmap()?.asImageBitmap()
                    ),
                    launchApp = ::launchApp,
                    killApp = ::killByPackageName,
                    hasRoot = viewModel.hasRoot()
                )
            }
        }
    }

    fun killByPackageName(packageName: String) {
        val packageInfo = packageManager?.getPackageInfo(packageName, 0)
        packageInfo?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val message =
                    if (viewModel.killByPackageInfo(it)) "Killed $packageName"
                    else "Failed to kill $packageName"
                // Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                Log.d("TAG", message)
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
    appList: List<App>,
    launchApp: (String) -> Unit,
    killApp: (String) -> Unit,
    hasRoot: Boolean
) {
    LazyColumn {
        items(items = appList) {
            RecentAppsItem(it.name, it.packageName, it.icon, launchApp, killApp, hasRoot)
        }
    }
}
