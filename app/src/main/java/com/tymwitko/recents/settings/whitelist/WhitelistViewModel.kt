package com.tymwitko.recents.settings.whitelist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.scottyab.rootbeer.RootBeer
import com.tymwitko.recents.common.accessors.AppsAccessor
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.settings.ui.UiSettingsHolder
import com.tymwitko.recents.settings.whitelist.db.WhitelistRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WhitelistViewModel(
  private val appsAccessor: AppsAccessor,
  private val whitelistRepository: WhitelistRepository,
  private val rootBeer: RootBeer,
  private val shizukuManager: ShizukuManager,
  private val uiSettingsHolder: UiSettingsHolder
) : ViewModel() {

  private val settings = HashMap<String, MutableLiveData<WhitelistSettingsData>>()

  private val _appList = MutableStateFlow<List<App>?>(null)
  
  val appList: StateFlow<List<App>?>
    get() = _appList

  fun refreshPackages(packageName: String) {
    CoroutineScope(Dispatchers.IO).launch {
      appsAccessor.getRecentApps(packageName, hasPrivileges()) // todo: add setting
        .filter { !appsAccessor.isLauncher(it.packageName) }
        .toSet()
        .onEach { app ->
          CoroutineScope(Dispatchers.IO).launch {
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
        }
        .let { _appList.value = it.toList() }
    }
  }

  fun whitelistAppLaunch(packageName: String, isChecked: Boolean) {
    CoroutineScope(Dispatchers.IO).launch {
      whitelistRepository.setLaunching(packageName, isChecked)
    }
  }

  fun whitelistAppKill(packageName: String, isChecked: Boolean) {
    CoroutineScope(Dispatchers.IO).launch {
      whitelistRepository.setKilling(packageName, isChecked)
    }
  }

  fun whitelistAppShow(packageName: String, isChecked: Boolean) {
    CoroutineScope(Dispatchers.IO).launch {
      whitelistRepository.setShowing(packageName, isChecked)
    }
  }

  fun getSettingsForApp(packageName: String) = settings[packageName]

  fun hasPrivileges() = shizukuManager.isShizukuAllowed() || rootBeer.isRooted
  
  fun getFontSize() = uiSettingsHolder.getFontSize()

  fun getIconSize(default: Int) = uiSettingsHolder.getIconSize(default)
}
