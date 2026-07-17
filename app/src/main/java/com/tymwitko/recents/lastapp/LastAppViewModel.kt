package com.tymwitko.recents.lastapp

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModel
import com.tymwitko.recents.common.exceptions.AppNotLaunchedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LastAppViewModel(
  private val launchLastAppUseCase: LaunchLastAppUseCase
) : ViewModel() {
  suspend fun launchLastApp(startActivity: (Intent, Bundle?) -> Unit, thisPackageName: String) {
    withContext(Dispatchers.Default) {
      if (!launchLastAppUseCase(thisPackageName, startActivity)) throw AppNotLaunchedException()
    }
  }
}
