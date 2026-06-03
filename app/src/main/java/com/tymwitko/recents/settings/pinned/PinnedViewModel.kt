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

  private val _appList = MutableStateFlow<List<App>?>(null)

  val appList: StateFlow<List<App>?>
    get() = _appList

  private val _isPinned = MutableStateFlow<List<PinnedAppDetails>?>(listOf())

  val isPinned: StateFlow<List<PinnedAppDetails>?>
    get() = _isPinned

  fun getIconSize(defaultSize: Int) = settingsHolder.getIconSize(defaultSize)

  fun getFontSize() = settingsHolder.getFontSize()

  fun fetchAppList(thisPackageName: String) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        _appList.update {
          getApps(thisPackageName, hasPrivileges(), isOnlyRunning())
        }
      }
    }
  }

  fun fetchSettings() {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        _isPinned.update {
          pinnedRepository.getAllPinned()
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
    hasPrivileges: Boolean,
    onlyRunning: Boolean
  ): MutableList<App> {
    return appsAccessor.getRecentApps(hasPrivileges, onlyRunning).toList()
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
        try {
          pinnedRepository.addPinned(PinnedAppDetails(app))
        } catch (_: SQLiteConstraintException) {
          pinnedRepository.removePinned(PinnedAppDetails(app))
        }
      }
    }
  }

  private fun isOnlyRunning() = settingsHolder.getOnlyRunning()

  private fun hasPrivileges() = shizukuManager.isShizukuAllowed() || rootBeer.isRooted
}