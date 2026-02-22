package com.tymwitko.recents

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.tymwitko.recents.exceptions.AppNotLaunchedException
import com.tymwitko.recents.viewmodels.LastAppViewModel
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
