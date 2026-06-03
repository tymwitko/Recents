package com.tymwitko.recents.entry

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class EntryActivity : AppCompatActivity() {
  private val viewModel by viewModel<EntryViewModel>()

  override fun onResume() {
    super.onResume()
    viewModel.launchDefault { activity ->
      startActivity(Intent(this@EntryActivity, activity))
    }
  }
}
