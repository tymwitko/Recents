package com.tymwitko.recents.lastapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.tymwitko.recents.recentapps.RecentAppsActivity
import com.tymwitko.recents.common.exceptions.AppNotLaunchedException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class LastAppActivity : AppCompatActivity() {
  private val viewModel by viewModel<LastAppViewModel>()

  override fun onResume() {
    super.onResume()
    try {
      CoroutineScope(Dispatchers.Default).launch {
        viewModel.launchLastApp(::startActivity, packageName)
      }
    } catch (e: AppNotLaunchedException) {
      startActivity(Intent(this, RecentAppsActivity::class.java))
    }
  }
}
