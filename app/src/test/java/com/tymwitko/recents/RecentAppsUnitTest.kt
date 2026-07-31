package com.tymwitko.recents

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.tymwitko.recents.common.FetchAppsUseCase
import com.tymwitko.recents.common.KillAppsUseCase
import com.tymwitko.recents.common.accessors.AppKiller
import com.tymwitko.recents.common.accessors.AppsAccessor
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.recentapps.RecentAppsUiState
import com.tymwitko.recents.recentapps.RecentAppsViewModel
import com.tymwitko.recents.recentapps.pinned.db.PinnedRepository
import com.tymwitko.recents.settings.SettingsHolder
import com.tymwitko.recents.settings.whitelist.WhitelistSettingsData
import com.tymwitko.recents.settings.whitelist.db.PackageSettings
import com.tymwitko.recents.settings.whitelist.db.WhitelistRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

@OptIn(ExperimentalCoroutinesApi::class)
class RecentAppsUnitTest {
  private val whitelistRepo: WhitelistRepository = mockk<WhitelistRepository>()
  private val appsAccessor: AppsAccessor = mockk<AppsAccessor>(relaxed = true)
  private val appKiller: AppKiller = mockk<AppKiller>(relaxed = true)
  private val shizukuManager: ShizukuManager = mockk<ShizukuManager>(relaxed = true)
  private val settingsHolder: SettingsHolder = mockk<SettingsHolder>(relaxed = true)
  private val pinnedRepository: PinnedRepository = mockk<PinnedRepository>()
  @get:Rule
  var rule: TestRule = InstantTaskExecutorRule()

  @Before
  fun `prepare tests`() {
    coEvery { pinnedRepository.getAllPinned() } returns listOf()
    coEvery {
      appsAccessor.getRecentApps(any(), any())
    } returns flowOf(
      App(
        name = "Recents",
        packageName = "com.tymwitko.recents",
        icon = null,
        lastTimeUsed = 0L,
        isRunning = true,
        isWorkApp = false
      ),
      App("Fake App","org.fake.app", null, 0L, true, false)
    )
    every { appsAccessor.isLauncher(any()) } returns false
    coEvery { whitelistRepo.getAllEntries() } returns listOf(
      PackageSettings(
        packageName = "com.tymwitko.recents",
        user = 0,
        canLaunch = true,
        canKill = true,
        canShow = true
      )
    ).associateBy({ it.getId() }, { it })
    coEvery { whitelistRepo.canShow(any()) } returns true
    every { appsAccessor.getAppName("com.tymwitko.recents") } returns "Recents"
    every { appsAccessor.getAppName("org.fake.app") } returns "Fake App"
    every { shizukuManager.isShizukuAllowed() } returns true
    every { settingsHolder.getOnlyRunning() } returns false
  }

  @Test
  fun `when getEntry called it should get settings`() {
    runTest {
      val viewModel = getViewModel(testScheduler)
      viewModel.fetchApps("com.tymwitko.recents")
      advanceUntilIdle()
      coVerify {
        whitelistRepo.getAllEntries()
      }
    }
  }

  @Test
  fun `getting all apps should return a list of apps`() {
    runTest {
      val viewModel = getViewModel(testScheduler)
      viewModel.fetchApps("com.tymwitko.recents")
      advanceUntilIdle()
      assertEquals(
        listOf(
          "org.fake.app" to "Fake App",
        ),
        (viewModel.uiState.value as? RecentAppsUiState.Success)?.list?.map {
          it.packageName to it.name
        }
      )
    }
  }

  @Test
  fun `killing apps should call appKiller`() {
    runTest {
      val viewModel = getViewModel(testScheduler)
      viewModel.fetchApps("com.tymwitko.recents")
      viewModel.killEmAll() {}
      advanceUntilIdle()
      coVerify(exactly = 1) { appKiller.killApp(any()) }
    }
  }

  @Test
  fun `whitelisting apps updates settings`() {
    coEvery { whitelistRepo.getAllEntries() } returns listOf(
      PackageSettings(
        packageName = "com.tymwitko.recents",
        user = 0,
        canLaunch = true,
        canKill = false,
        canShow = false
      ),
      PackageSettings(
        packageName = "org.fake.app",
        user = 0,
        canLaunch = true,
        canKill = false,
        canShow = true
      )
    ).associateBy({ it.getId() }, { it })
    runTest {
      val viewModel = getViewModel(testScheduler)
      viewModel.fetchApps("com.tymwitko.recents")
      advanceUntilIdle()
      assertEquals(
        WhitelistSettingsData(canLaunch = true, canKill = false, canShow = true),
        (viewModel.uiState.value as? RecentAppsUiState.Success)?.settings["org.fake.app0"]
      )
    }
  }

  @Test
  fun `whitelisting apps from showing stops them from being shown`() {
    coEvery { whitelistRepo.canShow("com.tymwitko.recents0") } returns false
    coEvery { whitelistRepo.canShow("org.fake.app0") } returns true
    runTest {
      val viewModel = getViewModel(testScheduler)
      viewModel.fetchApps("com.tymwitko.recents")
      advanceUntilIdle()
      assertEquals(
        listOf("Fake App" to "org.fake.app"),
        (viewModel.uiState.value as? RecentAppsUiState.Success)?.list
          ?.map { it.name to it.packageName }
      )
    }
  }

  private fun getViewModel(testScheduler: TestCoroutineScheduler) = RecentAppsViewModel(

    KillAppsUseCase(
      appKiller,
      appsAccessor,
      shizukuManager,
      mockk(relaxed = true)
    ),
    mockk(),
    whitelistRepo,
    FetchAppsUseCase(
      appsAccessor,
      whitelistRepo,
      mockk(relaxed = true),
      settingsHolder,
      shizukuManager,
      mockk(relaxed = true)
    ),
    shizukuManager,
    settingsHolder,
    mockk(relaxed = true),
    mockk(relaxed = true),
    StandardTestDispatcher(testScheduler)
  )
}
