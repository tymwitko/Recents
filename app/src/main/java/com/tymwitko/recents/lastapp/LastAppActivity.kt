package com.tymwitko.recents.lastapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tymwitko.recents.recentapps.RecentAppsActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class LastAppActivity : AppCompatActivity() {
  private val viewModel by viewModel<LastAppViewModel>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel.launchLastApp(::startActivity, ::finish, ::launchAppList)
  }

  override fun onResume() {
    super.onResume()
    launchAppList()
  }

  private fun launchAppList() {
    startActivity(Intent(this, RecentAppsActivity::class.java))
  }
}
