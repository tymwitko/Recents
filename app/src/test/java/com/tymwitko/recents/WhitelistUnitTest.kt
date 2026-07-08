package com.tymwitko.recents

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

class WhitelistUnitTest {
  private val whitelistRepo: WhitelistRepository = mockk<WhitelistRepository>()
  private val appsAccessor: AppsAccessor = mockk<AppsAccessor>()
  private val shizukuManager: ShizukuManager = mockk<ShizukuManager>()
  private val settingsHolder: SettingsHolder = mockk<SettingsHolder>()
  @get:Rule
  var rule: TestRule = InstantTaskExecutorRule()
  @OptIn(ExperimentalCoroutinesApi::class)
  val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
  val SLEEP = 1000L

  val viewModel = WhitelistViewModel(
    appsAccessor,
    whitelistRepo,
    mockk(),
    shizukuManager,
    settingsHolder
  )

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
    coEvery { whitelistRepo.getEntry(any()) } returns PackageSettings(
      packageName = "ai.is.theft",
      user = 0,
      canLaunch = true,
      canKill = true,
      canShow = true
    )
    every { appsAccessor.getAppName("ai.is.theft") } returns "Github Copilot"
    every { appsAccessor.getAppName("org.fake.app") } returns "Fake App"
    every { shizukuManager.isShizukuAllowed() } returns true
  }
  
  @Test
  fun `when all packages queried called it should getEntry`() {
    runTest {
      viewModel.refreshPackages("com.tymwitko.recents")
      Thread.sleep(SLEEP)
      coVerify { 
        whitelistRepo.getEntry("ai.is.theft0")
      }
    }
  }
  
  @Test
  fun `getting all apps should return a list of apps`() {
    runTest {
      viewModel.refreshPackages("com.tymwitko.recents")
      Thread.sleep(SLEEP)
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
      viewModel.refreshPackages("com.tymwitko.recents")
      Thread.sleep(SLEEP)
      val settings = (viewModel.uiState.value as? WhitelistUiState.Success)?.settings["ai.is.theft0"]
      assertEquals(WhitelistSettingsData(true, true, true), settings)
    }
  }
  
  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `whitelisting apps updates settings`() {
    coEvery { whitelistRepo.getEntry("ai.is.theft0") } returns PackageSettings(
      packageName = "ai.is.theft",
      user = 0,
      canLaunch = true,
      canKill = false,
      canShow = false
    )
    Dispatchers.setMain(testDispatcher)
    runTest {
      viewModel.refreshPackages("com.tymwitko.recents")
      Thread.sleep(SLEEP)
      assertEquals(
        WhitelistSettingsData(true, false, false),
        (viewModel.uiState.value as? WhitelistUiState.Success)?.settings["ai.is.theft0"]
      )
    }
  }
}