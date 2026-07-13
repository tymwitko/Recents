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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import kotlin.time.Duration.Companion.milliseconds

internal const val SLEEP = 1000L

class RecentAppsUnitTest {
  private val whitelistRepo: WhitelistRepository = mockk<WhitelistRepository>()
  private val appsAccessor: AppsAccessor = mockk<AppsAccessor>(relaxed = true)
  private val appKiller: AppKiller = mockk<AppKiller>(relaxed = true)
  private val shizukuManager: ShizukuManager = mockk<ShizukuManager>(relaxed = true)
  private val settingsHolder: SettingsHolder = mockk<SettingsHolder>(relaxed = true)
  private val pinnedRepository: PinnedRepository = mockk<PinnedRepository>()
  @get:Rule
  var rule: TestRule = InstantTaskExecutorRule()
  @OptIn(ExperimentalCoroutinesApi::class)
  val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

  val viewModel = RecentAppsViewModel(
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
    settingsHolder
  )

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
    coEvery { whitelistRepo.getEntry(any()) } returns PackageSettings(
      packageName = "com.tymwitko.recents",
      user = 0,
      canLaunch = true,
      canKill = true,
      canShow = true
    )
    coEvery { whitelistRepo.canShow(any()) } returns true
    every { appsAccessor.getAppName("com.tymwitko.recents") } returns "Recents"
    every { appsAccessor.getAppName("org.fake.app") } returns "Fake App"
    every { shizukuManager.isShizukuAllowed() } returns true
    every { settingsHolder.getOnlyRunning() } returns false
  }

  @Test
  fun `when getEntry called it should get settings`() {
    runTest {
      viewModel.fetchApps("com.tymwitko.recents")
      delay(SLEEP.milliseconds)
      coVerify { 
        whitelistRepo.getEntry("org.fake.app0")
      }
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `getting all apps should return a list of apps`() {
    runTest {
      viewModel.fetchApps("com.tymwitko.recents")
      Thread.sleep(SLEEP)
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

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `killing apps should call appKiller`() {
    Dispatchers.setMain(testDispatcher)
    runTest {
      viewModel.fetchApps("com.tymwitko.recents")
      viewModel.killEmAll("com.tymwitko.recents") {}
      coVerify(exactly = 1) { appKiller.killApp(any()) }
    }
  }

  @Test
  fun `whitelisting apps updates settings`() {
    coEvery { whitelistRepo.getEntry("com.tymwitko.recents0") } returns PackageSettings(
      packageName = "com.tymwitko.recents",
      user = 0,
      canLaunch = true,
      canKill = false,
      canShow = false
    )
    coEvery { whitelistRepo.getEntry("org.fake.app0") } returns PackageSettings(
      packageName = "org.fake.app",
      user = 0,
      canLaunch = true,
      canKill = false,
      canShow = true
    )
    runTest {
      viewModel.fetchApps("com.tymwitko.recents")
      Thread.sleep(SLEEP)
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
      viewModel.fetchApps("com.tymwitko.recents")
      Thread.sleep(SLEEP)
      assertEquals(
        listOf("Fake App" to "org.fake.app"),
        (viewModel.uiState.value as? RecentAppsUiState.Success)?.list
          ?.map { it.name to it.packageName }
      )
    }   
  }
}
