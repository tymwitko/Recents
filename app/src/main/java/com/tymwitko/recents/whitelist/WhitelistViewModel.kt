package com.tymwitko.recents.whitelist

import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.scottyab.rootbeer.RootBeer
import com.tymwitko.recents.common.accessors.AppsAccessor
import com.tymwitko.recents.common.accessors.IconAccessor
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.whitelist.db.WhitelistRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WhitelistViewModel(
  private val appsAccessor: AppsAccessor,
  private val iconAccessor: IconAccessor,
  private val whitelistRepository: WhitelistRepository,
  private val rootBeer: RootBeer
) : ViewModel() {

  private val settings = HashMap<String, MutableLiveData<WhitelistSettings>>()

  fun getAllPackages(packageName: String, placeHolderIcon: ImageBitmap?) =
    appsAccessor.getRecentAppsFormatted(
      packageName
    )
      .filter { !appsAccessor.isLauncher(it) }.toSet().also {
        if (it.isEmpty()) {
          Log.d("TAG", "List empty")
        }
        it.forEachIndexed { ind, t ->
          Log.d("TAG", "App $ind is $t")
        }
      }.map {
        App(
          appsAccessor.getAppName(it).orEmpty(),
          it,
          iconAccessor.getAppIcon(it) ?: placeHolderIcon ?: throw NoSuchElementException()
      ).also {
        CoroutineScope(Dispatchers.IO).launch {
          it.packageName.let { name ->
            settings[name] = MutableLiveData<WhitelistSettings>()
            whitelistRepository.getEntry(name)?.let { entry ->
              settings[name]?.postValue(
                WhitelistSettings(
                  entry.canLaunch,
                  entry.canKill,
                  entry.canShow
                )
              )
            }
          }
        }
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

  fun hasRoot() = rootBeer.isRooted
}
