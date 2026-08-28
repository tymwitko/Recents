package com.tymwitko.recents.settings.pinned

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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PinnedViewModel(
  private val settingsHolder: SettingsHolder,
  private val pinnedRepository: PinnedRepository,
  private val fetchAppsUseCase: FetchAppsUseCase,
  private val fetchPinnedAppsUseCase: FetchPinnedAppsUseCase,
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

  val uiState: StateFlow<PinnedSettingsUiState>
    field = MutableStateFlow<PinnedSettingsUiState>(PinnedSettingsUiState.Loading)

  fun getIconSize(defaultSize: Int) = settingsHolder.getIconSize(defaultSize)

  fun getFontSize() = settingsHolder.getFontSize()

  fun fetchAppList() {
    viewModelScope.launch(dispatcher) {
      when (val result = fetchAppsUseCase(withFilter = false, withPinned = true)) {
        is Result.Failure -> {
          uiState.emit(
            PinnedSettingsUiState.Error(IllegalStateException("List empty!"))
          )
        }

        is Result.Success -> {
          val pinnedResult = fetchPinnedAppsUseCase(result.data.apps)
          uiState.emit(
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
        uiState.update { old ->
          (old as? PinnedSettingsUiState.Success)?.copy(
            pinned = old.pinned.plus(app)
          ) ?: old
        }
      } catch (_: SQLiteConstraintException) {
        pinnedRepository.removePinned(PinnedAppDetails(app))
        uiState.update { old ->
          (old as? PinnedSettingsUiState.Success)?.copy(
            pinned = old.pinned.minus(app)
          ) ?: old
        }
      }
    }
  }
}
