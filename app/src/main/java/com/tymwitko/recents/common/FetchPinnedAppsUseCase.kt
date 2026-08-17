package com.tymwitko.recents.common

import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.recentapps.pinned.db.PinnedAppDetails
import com.tymwitko.recents.recentapps.pinned.db.PinnedRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class FetchPinnedAppsUseCase(private val pinnedRepository: PinnedRepository) {
  suspend operator fun invoke(fullList: List<App>): Result<List<App>, Error> =
    coroutineScope {
      val pinnedDeferred = async {
        val pinned = pinnedRepository.getAllPinned()
        fullList.filter {
          PinnedAppDetails(it) in pinned
        }.toMutableList()
      }

      val pinnedApps = pinnedDeferred.await()
      if (pinnedApps.isNotEmpty()) Result.Success(pinnedApps)
      else Result.Failure(EmptyError())
    }
}
