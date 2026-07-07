package com.tymwitko.recents.settings.pinned

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scottyab.rootbeer.RootBeer
import com.tymwitko.recents.common.accessors.AppsAccessor
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.common.distinctByNamePickApp
import com.tymwitko.recents.recentapps.pinned.db.PinnedAppDetails
import com.tymwitko.recents.recentapps.pinned.db.PinnedRepository
import com.tymwitko.recents.settings.SettingsHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PinnedViewModel(
  private val settingsHolder: SettingsHolder,
  private val appsAccessor: AppsAccessor,
  private val shizukuManager: ShizukuManager,
  private val rootBeer: RootBeer,
  private val pinnedRepository: PinnedRepository
) : ViewModel() {

  private val _uiState = MutableStateFlow<PinnedSettingsUiState>(PinnedSettingsUiState.Loading)
  val uiState: StateFlow<PinnedSettingsUiState> = _uiState.asStateFlow()

  fun getIconSize(defaultSize: Int) = settingsHolder.getIconSize(defaultSize)

  fun getFontSize() = settingsHolder.getFontSize()

  fun fetchAppList(thisPackageName: String) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        try {
          val list = getApps(thisPackageName, hasPrivileges())
          val pinned = pinnedRepository.getAllPinned()
          _uiState.emit(
            if (list.isNotEmpty()) PinnedSettingsUiState.Success(
              list = list,
              pinned = pinned
            ) else PinnedSettingsUiState.Error(
              "List empty, but no error was thrown!"
            )
          )
        } catch (e: Exception) {
          _uiState.emit(
            PinnedSettingsUiState.Error(e.stackTraceToString())
          )
        }
      }
    }
  }

  fun isPinnedByApp(app: App, arePinned: List<PinnedAppDetails>?) =
    arePinned?.any {
      it.packageName == app.packageName && it.user == if (app.isWorkApp) 10 else 0
    } == true

  private suspend fun getApps(
    thisPackageName: String,
    hasPrivileges: Boolean
  ): MutableList<App> {
    return appsAccessor.getRecentApps(hasPrivileges, false).toList()
      .filter {
        it.packageName != thisPackageName && !appsAccessor.isLauncher(it.packageName)
      }
      .distinctByNamePickApp()
      .sortedByDescending { it.lastTimeUsed }
      .toMutableList()
  }

  fun pinOrUnpinApp(app: App) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        PinnedAppDetails(app).let { det ->
          try {
            pinnedRepository.addPinned(det)
            _uiState.update { old ->
              (old as? PinnedSettingsUiState.Success)?.copy(
                pinned = old.pinned.plus(det)
              ) ?: old
            }
          } catch (_: SQLiteConstraintException) {
            pinnedRepository.removePinned(det)
            _uiState.update { old ->
              (old as? PinnedSettingsUiState.Success)?.copy(
                pinned = old.pinned.minus(det)
              ) ?: old
            }
          }
        }
      }
    }
  }

  private fun hasPrivileges() = shizukuManager.isShizukuAllowed() || rootBeer.isRooted
}