package com.tymwitko.recents

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.tymwitko.recents.common.FetchAppsUseCase
import com.tymwitko.recents.common.accessors.AppsAccessor
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.settings.SettingsHolder
import com.tymwitko.recents.settings.whitelist.WhitelistSettingsData
import com.tymwitko.recents.settings.whitelist.WhitelistUiState
import com.tymwitko.recents.settings.whitelist.WhitelistViewModel
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
class WhitelistUnitTest {
  private val whitelistRepo: WhitelistRepository = mockk<WhitelistRepository>()
  private val appsAccessor: AppsAccessor = mockk<AppsAccessor>()
  private val shizukuManager: ShizukuManager = mockk<ShizukuManager>()
  private val settingsHolder: SettingsHolder = mockk<SettingsHolder>()

  @get:Rule
  var rule: TestRule = InstantTaskExecutorRule()

  @Before
  fun `prepare tests`() {
    every { settingsHolder.getOnlyRunning() } returns false
    coEvery {
      appsAccessor.getRecentApps(any(), any())
    } returns flowOf(
      App("Github Copilot", "ai.is.theft", null, 0L, true, false),
      App("Fake App", "org.fake.app", null, 0L, true, false)
    )
    every { appsAccessor.isLauncher(any()) } returns false
    coEvery { whitelistRepo.getAllEntries() } returns listOf(
      PackageSettings(
        packageName = "ai.is.theft",
        user = 0,
        canLaunch = true,
        canKill = true,
        canShow = true
      )
    ).associateBy({ it.getId() }, { it })
    every { appsAccessor.getAppName("ai.is.theft") } returns "Github Copilot"
    every { appsAccessor.getAppName("org.fake.app") } returns "Fake App"
    every { shizukuManager.isShizukuAllowed() } returns true
    every { settingsHolder.isOrderReversed() } returns false
  }

  @Test
  fun `when all packages queried called it should getEntry`() {
    runTest {
      val viewModel = getViewModel(testScheduler)
      viewModel.refreshPackages()
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
      viewModel.refreshPackages()
      advanceUntilIdle()
      val apps = (viewModel.uiState.value as? WhitelistUiState.Success)?.list
      assertEquals(
        listOf(
          "ai.is.theft" to "Github Copilot",
          "org.fake.app" to "Fake App"
        ),
        apps?.map {
          it.packageName to it.name
        }
      )
    }
  }

  @Test
  fun `getting settings apps should return saved settings`() {
    runTest {
      val viewModel = getViewModel(testScheduler)
      viewModel.refreshPackages()
      advanceUntilIdle()
      val settings =
        (viewModel.uiState.value as? WhitelistUiState.Success)?.settings["ai.is.theft0"]
      assertEquals(WhitelistSettingsData(true, true, true), settings)
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `whitelisting apps updates settings`() {
    coEvery { whitelistRepo.getAllEntries() } returns listOf(
      PackageSettings(
        packageName = "ai.is.theft",
        user = 0,
        canLaunch = true,
        canKill = false,
        canShow = false
      )
    ).associateBy({ it.getId() }, { it })
    runTest {
      val viewModel = getViewModel(testScheduler)
      viewModel.refreshPackages()
      advanceUntilIdle()
      assertEquals(
        WhitelistSettingsData(true, false, false),
        (viewModel.uiState.value as? WhitelistUiState.Success)?.settings["ai.is.theft0"]
      )
    }
  }

  private fun getViewModel(testScheduler: TestCoroutineScheduler): WhitelistViewModel {

    val testDispatcher = StandardTestDispatcher(testScheduler)

    return WhitelistViewModel(
      whitelistRepo,
      settingsHolder,
      FetchAppsUseCase(
        appsAccessor,
        whitelistRepo,
        mockk(relaxed = true),
        settingsHolder,
        shizukuManager
      ),
      mockk(relaxed = true),
      testDispatcher
    )
  }
}
