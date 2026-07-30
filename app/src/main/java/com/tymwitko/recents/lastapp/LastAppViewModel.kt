package com.tymwitko.recents.lastapp

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tymwitko.recents.common.exceptions.AppNotLaunchedException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LastAppViewModel(
  private val launchLastAppUseCase: LaunchLastAppUseCase,
  private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {
  fun launchLastApp(
    startActivity: (Intent, Bundle?) -> Unit,
    thisPackageName: String,
    onSuccess: () -> Unit
  ) {
    viewModelScope.launch(dispatcher) {
      if (launchLastAppUseCase(thisPackageName, startActivity)) onSuccess()
      else throw AppNotLaunchedException()
    }
  }
}
