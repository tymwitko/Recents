package com.tymwitko.recents.viewmodels

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.tymwitko.recents.accessors.IntentSender
import com.tymwitko.recents.accessors.AppsAccessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent

class LastAppViewModel(
  private val intentSender: IntentSender,
  private val appsAccessor: AppsAccessor
) : ViewModel() {

  suspend fun launchLastApp(startActivity: (Intent) -> Unit, thisPackageName: String) {
    withContext(Dispatchers.Default) {
      appsAccessor.getRecentAppsFormatted(thisPackageName)
        .drop(1)
        .filter { !appsAccessor.shouldLaunch(it) }
        .let { intentSender.launchLastApp(it, startActivity) }
    }
  }
}
