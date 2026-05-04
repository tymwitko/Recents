package com.tymwitko.recents

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.tymwitko.recents.common.accessors.AppsAccessor
import com.tymwitko.recents.common.accessors.IconAccessor
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.settings.whitelist.WhitelistSettingsData
import com.tymwitko.recents.settings.whitelist.WhitelistViewModel
import com.tymwitko.recents.settings.whitelist.db.WhitelistRepository
import com.tymwitko.recents.whitelist.db.PackageSettings
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
  private val iconAccessor: IconAccessor = mockk<IconAccessor>(relaxed = true)
  private val shizukuManager: ShizukuManager = mockk<ShizukuManager>()
  @get:Rule
  var rule: TestRule = InstantTaskExecutorRule()
  @OptIn(ExperimentalCoroutinesApi::class)
  val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
  val SLEEP = 1000L

  val viewModel = WhitelistViewModel(
    appsAccessor,
    iconAccessor,
    whitelistRepo,
    mockk(),
    shizukuManager,
    mockk()
  )

  @Before
  fun `prepare tests`() {

    coEvery {
      appsAccessor.getAppsViaUsageStatsManager(any(), any())
    } returns listOf(
      App("Recents", "com.tymwitko.recents", null),
      App("Fake App", "org.fake.app", null)
    )
    every { appsAccessor.isLauncher(any()) } returns false
    coEvery { whitelistRepo.getEntry(any()) } returns PackageSettings(
      "com.tymwitko.recents",
      true,
      true,
      true
    )
    every { appsAccessor.getAppName("com.tymwitko.recents") } returns "Recents"
    every { appsAccessor.getAppName("org.fake.app") } returns "Fake App"
    every { shizukuManager.isShizukuAllowed() } returns true
  }
  
  @Test
  fun `when all packages queried called it should getEntry`() {
    runTest {
      viewModel.refreshPackages("com.tymwitko.recents")
      Thread.sleep(SLEEP)
      coVerify { 
        whitelistRepo.getEntry("com.tymwitko.recents")
      }
    }
  }
  
  @Test
  fun `getting all apps should return a list of apps`() {
    runTest {
      viewModel.refreshPackages("com.tymwitko.recents")
      Thread.sleep(SLEEP)
      val apps = viewModel.appList.value
      assertEquals(
        listOf(
          "com.tymwitko.recents" to "Recents",
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
      val settings = viewModel.getSettingsForApp("com.tymwitko.recents")?.value
      assertEquals(WhitelistSettingsData(true, true, true), settings)
    }
  }
  
  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `whitelisting apps updates settings`() {
    coEvery { whitelistRepo.getEntry("com.tymwitko.recents") } returns PackageSettings(
      "com.tymwitko.recents",
      true, false, false
    )
    coEvery { whitelistRepo.getEntry("com.tymwitko.recents") } returns PackageSettings(
      "com.tymwitko.recents",
      true, false, false
    )
    Dispatchers.setMain(testDispatcher)
    runTest {
      viewModel.refreshPackages("com.tymwitko.recents")
      Thread.sleep(SLEEP)
      assertEquals(
        WhitelistSettingsData(true, false, false),
        viewModel.getSettingsForApp("com.tymwitko.recents")?.value
      )
    }
  }
}