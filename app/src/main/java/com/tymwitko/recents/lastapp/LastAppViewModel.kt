package com.tymwitko.recents.lastapp

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.tymwitko.recents.common.accessors.AppsAccessor
import com.tymwitko.recents.common.accessors.IntentSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext

class LastAppViewModel(
  private val intentSender: IntentSender,
  private val appsAccessor: AppsAccessor
) : ViewModel() {

  suspend fun launchLastApp(startActivity: (Intent) -> Unit, thisPackageName: String) {
    withContext(Dispatchers.Default) {
      appsAccessor.getRecentApps(false)
        .filter { it.packageName != thisPackageName }
        .toList()
        .sortedByDescending { it.lastTimeUsed }
        .drop(1)
        .filter { appsAccessor.shouldLaunch(it.packageName) }
        .let { intentSender.launchLastApp(it.toList(), startActivity) }
    }
  }
}
