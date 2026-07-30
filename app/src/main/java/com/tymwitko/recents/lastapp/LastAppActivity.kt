package com.tymwitko.recents.lastapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.tymwitko.recents.recentapps.RecentAppsActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class LastAppActivity : AppCompatActivity() {
  private val viewModel by viewModel<LastAppViewModel>()

  override fun onResume() {
    super.onResume()
    viewModel.launchLastApp(::startActivity, packageName, ::finish) {
      startActivity(Intent(this@LastAppActivity, RecentAppsActivity::class.java))
    }
  }
}
