package com.tymwitko.recents.settings.whitelist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scottyab.rootbeer.RootBeer
import com.tymwitko.recents.common.accessors.AppsAccessor
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.settings.SettingsHolder
import com.tymwitko.recents.settings.whitelist.db.WhitelistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WhitelistViewModel(
  private val appsAccessor: AppsAccessor,
  private val whitelistRepository: WhitelistRepository,
  private val rootBeer: RootBeer,
  private val shizukuManager: ShizukuManager,
  private val settingsHolder: SettingsHolder
) : ViewModel() {

  private val settings = HashMap<String, MutableLiveData<WhitelistSettingsData>>()

  private val _appList = MutableStateFlow<List<App>?>(null)

  val appList: StateFlow<List<App>?>
    get() = _appList

  private val _hasPrivileges = MutableStateFlow(false)
  val hasPrivileges: StateFlow<Boolean>
    get() = _hasPrivileges

  fun refreshPackages(thisPackageName: String) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        appsAccessor.getRecentApps(hasPrivileges.value, settingsHolder.getOnlyRunning())
          .let {
            it.toList()
              .distinctBy { it.packageName to it.isWorkApp }
              .filter { it.packageName != thisPackageName && !appsAccessor.isLauncher(it.packageName) }
              .onEach { app ->
                settings[app.packageName] = MutableLiveData()
                whitelistRepository.getEntry(app.packageName)?.let { packageSettings ->
                  settings[app.packageName]?.postValue(
                    WhitelistSettingsData(
                      packageSettings.canLaunch,
                      packageSettings.canKill,
                      packageSettings.canShow
                    )
                  )
                }
              }
              .let { _appList.value = it }
          }
      }
    }
  }

  fun whitelistAppLaunch(packageName: String, isChecked: Boolean) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        whitelistRepository.setLaunching(packageName, isChecked)
      }
    }
  }

  fun whitelistAppKill(packageName: String, isChecked: Boolean) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        whitelistRepository.setKilling(packageName, isChecked)
      }
    }
  }

  fun whitelistAppShow(packageName: String, isChecked: Boolean) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        whitelistRepository.setShowing(packageName, isChecked)
      }
    }
  }

  fun getSettingsForApp(packageName: String) = settings[packageName]

  fun checkPrivileges() {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        _hasPrivileges.update {
          shizukuManager.isShizukuAllowed() || rootBeer.isRooted
        }
      }
    }
  }

  fun getFontSize() = settingsHolder.getFontSize()

  fun getIconSize(default: Int) = settingsHolder.getIconSize(default)
}
