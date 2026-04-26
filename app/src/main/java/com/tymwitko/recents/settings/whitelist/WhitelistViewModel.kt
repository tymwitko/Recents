package com.tymwitko.recents.settings.whitelist

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.scottyab.rootbeer.RootBeer
import com.tymwitko.recents.common.accessors.AppsAccessor
import com.tymwitko.recents.common.accessors.IconAccessor
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.settings.ui.UiSettingsHolder
import com.tymwitko.recents.settings.whitelist.db.WhitelistRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WhitelistViewModel(
  private val appsAccessor: AppsAccessor,
  private val iconAccessor: IconAccessor,
  private val whitelistRepository: WhitelistRepository,
  private val rootBeer: RootBeer,
  private val shizukuManager: ShizukuManager,
  private val uiSettingsHolder: UiSettingsHolder
) : ViewModel() {

  private val settings = HashMap<String, MutableLiveData<WhitelistSettingsData>>()

  private val _appList = MutableLiveData<List<App>>()
  
  val appList: LiveData<List<App>>
    get() = _appList

  fun getAllPackages(packageName: String, placeHolderIcon: ImageBitmap?) {
    CoroutineScope(Dispatchers.IO).launch {
      appsAccessor.getRecentAppsFormatted(packageName)
        .filter { !appsAccessor.isLauncher(it) }.toSet()
        .map { name ->
          App(
            appsAccessor.getAppName(name).orEmpty(),
            name,
            iconAccessor.getAppIcon(name) ?: placeHolderIcon ?: throw NoSuchElementException()
          ).also {
            CoroutineScope(Dispatchers.IO).launch {
              settings[name] = MutableLiveData()
              whitelistRepository.getEntry(name)?.let { packageSettings ->
                settings[name]?.postValue(
                  WhitelistSettingsData(
                    packageSettings.canLaunch,
                    packageSettings.canKill,
                    packageSettings.canShow
                  )
                )
              }
            }
          }
        }
        .let(_appList::postValue)
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
