package com.tymwitko.recents.settings.pinned

import android.content.ClipData
import android.content.ClipboardManager
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tymwitko.recents.common.FetchAppsUseCase
import com.tymwitko.recents.common.FetchPinnedAppsUseCase
import com.tymwitko.recents.common.Result
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.recentapps.pinned.db.PinnedAppDetails
import com.tymwitko.recents.recentapps.pinned.db.PinnedRepository
import com.tymwitko.recents.settings.SettingsHolder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PinnedViewModel(
  private val settingsHolder: SettingsHolder,
  private val pinnedRepository: PinnedRepository,
  private val fetchAppsUseCase: FetchAppsUseCase,
  private val fetchPinnedAppsUseCase: FetchPinnedAppsUseCase,
  private val clipboardManager: ClipboardManager,
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

  private val _uiState = MutableStateFlow<PinnedSettingsUiState>(PinnedSettingsUiState.Loading)
  val uiState: StateFlow<PinnedSettingsUiState> = _uiState.asStateFlow()

  fun getIconSize(defaultSize: Int) = settingsHolder.getIconSize(defaultSize)

  fun getFontSize() = settingsHolder.getFontSize()

  fun fetchAppList() {
    viewModelScope.launch(dispatcher) {
      when (val result = fetchAppsUseCase(withFilter = false, withPinned = true)) {
        is Result.Failure -> {
          _uiState.emit(
            PinnedSettingsUiState.Error(IllegalStateException("List empty!"))
          )
        }

        is Result.Success -> {
          val pinnedResult = fetchPinnedAppsUseCase(result.data.apps)
          _uiState.emit(
            PinnedSettingsUiState.Success(
              list = result.data.apps,
              pinned = (pinnedResult as? Result.Success)?.data ?: emptyList()
            )
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
    viewModelScope.launch(dispatcher) {
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

  fun copyToClipboard(content: String) {
    val copy = ClipData.newPlainText("", content)
    clipboardManager.setPrimaryClip(copy)
  }
}
