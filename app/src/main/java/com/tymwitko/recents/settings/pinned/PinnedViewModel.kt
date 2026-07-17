package com.tymwitko.recents.settings.pinned

import android.content.ClipData
import android.content.ClipboardManager
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tymwitko.recents.common.FetchAppsUseCase
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.recentapps.pinned.db.PinnedAppDetails
import com.tymwitko.recents.recentapps.pinned.db.PinnedRepository
import com.tymwitko.recents.settings.SettingsHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PinnedViewModel(
  private val settingsHolder: SettingsHolder,
  private val pinnedRepository: PinnedRepository,
  private val fetchAppsUseCase: FetchAppsUseCase,
  private val clipboardManager: ClipboardManager
) : ViewModel() {

  private val _uiState = MutableStateFlow<PinnedSettingsUiState>(PinnedSettingsUiState.Loading)
  val uiState: StateFlow<PinnedSettingsUiState> = _uiState.asStateFlow()

  fun getIconSize(defaultSize: Int) = settingsHolder.getIconSize(defaultSize)

  fun getFontSize() = settingsHolder.getFontSize()

  fun fetchAppList(thisPackageName: String) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        try {
          val appData = fetchAppsUseCase(thisPackageName, withFilter = false, withPinned = true)
          _uiState.emit(
            if (appData.apps.isNotEmpty()) PinnedSettingsUiState.Success(
              list = appData.apps,
              pinned = appData.pinned
            ) else PinnedSettingsUiState.Error(
              IllegalStateException("List empty!")
            )
          )
        } catch (e: Exception) {
          _uiState.emit(
            PinnedSettingsUiState.Error(e)
          )
        }
      }
    }
  }

  fun isPinnedByApp(app: App, arePinned: List<App>?) =
    arePinned?.any {
      it.getId() == app.getId()
    } == true

  fun pinOrUnpinApp(app: App) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        try {
          pinnedRepository.addPinned(PinnedAppDetails(app))
          _uiState.update { old ->
            (old as? PinnedSettingsUiState.Success)?.copy(
              pinned = old.pinned.plus(app)
            ) ?: old
          }
        } catch (_: SQLiteConstraintException) {
          pinnedRepository.removePinned(PinnedAppDetails(app))
          _uiState.update { old ->
            (old as? PinnedSettingsUiState.Success)?.copy(
              pinned = old.pinned.minus(app)
            ) ?: old
          }
        }
      }
    }
  }
  
  fun copyToClipboard(content: String) {
    val copy = ClipData.newPlainText("", content)
    clipboardManager.setPrimaryClip(copy)
  }
}
