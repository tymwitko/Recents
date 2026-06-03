package com.tymwitko.recents.entry

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class EntryActivity : AppCompatActivity() {
  private val viewModel by viewModel<EntryViewModel>()

  override fun onResume() {
    super.onResume()
    lifecycleScope.launch {
      withContext(Dispatchers.IO) {
        viewModel.launchDefault { activity ->
          startActivity(Intent(this@EntryActivity, activity))
        }
      }
    }
  }
}
