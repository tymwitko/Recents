package com.tymwitko.recents

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.tymwitko.recents.common.accessors.AppKiller
import com.tymwitko.recents.common.accessors.AppsAccessor
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.recentapps.RecentAppsViewModel
import com.tymwitko.recents.settings.whitelist.WhitelistSettingsData
import com.tymwitko.recents.settings.whitelist.db.WhitelistRepository
import com.tymwitko.recents.whitelist.db.PackageSettings
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

class RecentAppsUnitTest {
  private val whitelistRepo: WhitelistRepository = mockk<WhitelistRepository>()
  private val appsAccessor: AppsAccessor = mockk<AppsAccessor>(relaxed = true)
  private val appKiller: AppKiller = mockk<AppKiller>(relaxed = true)
  private val shizukuManager: ShizukuManager = mockk<ShizukuManager>(relaxed = true)
  @get:Rule
  var rule: TestRule = InstantTaskExecutorRule()
  @OptIn(ExperimentalCoroutinesApi::class)
  val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
  val SLEEP = 1000L

  val viewModel = RecentAppsViewModel(
    appsAccessor,
    appKiller,
    mockk(),
    mockk(),
    whitelistRepo,
    shizukuManager,
    mockk()
  )

  @Before
  fun `prepare tests`() {

    coEvery {
      appsAccessor.getRecentApps(any())
    } returns flowOf(
      App("Recents","com.tymwitko.recents", null, 0L),
      App("Fake App","org.fake.app", null, 0L)
    )
    every { appsAccessor.isLauncher(any()) } returns false
    coEvery { whitelistRepo.getEntry(any()) } returns PackageSettings(
      "com.tymwitko.recents",
      true,
      true,
      true
    )
    coEvery { whitelistRepo.canShow(any()) } returns true
    every { appsAccessor.getAppName("com.tymwitko.recents") } returns "Recents"
    every { appsAccessor.getAppName("org.fake.app") } returns "Fake App"
    every { shizukuManager.isShizukuAllowed() } returns true
  }
  
  @Test
  fun `when getEntry called it should get settings`() {
    runTest {
      val apps = viewModel.getActiveApps("com.tymwitko.recents")
      coVerify { 
        whitelistRepo.getEntry("org.fake.app")
      }
    }
  }
  
  @Test
  fun `getting all apps should return a list of apps`() {
    runTest {
      val apps = viewModel.getActiveApps("com.tymwitko.recents")
      assertEquals(
        listOf(
          "org.fake.app" to "Fake App",
        ),
        apps.map {
          it.packageName to it.name
        }
      )
    }
  }

  @Test
  fun `getting settings apps should return saved settings`() {
    runTest {
      val apps = viewModel.getActiveApps("com.tymwitko.recents")
      Thread.sleep(SLEEP)
      val settings = viewModel.getSettingsForApp("org.fake.app")?.value
      assertEquals(WhitelistSettingsData(true, true, true), settings)
    }
  }
  
  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `killing apps should call appKiller`() {
    Dispatchers.setMain(testDispatcher)
    runTest {
      val apps = viewModel.getActiveApps("com.tymwitko.recents")
      Thread.sleep(SLEEP)
      viewModel.killEmAll("com.tymwitko.recents") {}
      coVerify(exactly = 1) { appKiller.killByPackageName(any()) }
    }
  }

  @Test
  fun `whitelisting apps updates settings`() {
    coEvery { whitelistRepo.getEntry("com.tymwitko.recents") } returns PackageSettings(
      "com.tymwitko.recents",
      true, false, false
    )
    coEvery { whitelistRepo.getEntry("org.fake.app") } returns PackageSettings(
      "org.fake.app",
      true, false, true
    )
    runTest {
      val apps = viewModel.getActiveApps("com.tymwitko.recents")
      Thread.sleep(SLEEP)
      assertEquals(
        WhitelistSettingsData(true, false, true),
        viewModel.getSettingsForApp("org.fake.app")?.value
      )
    }
  }

  @Test
  fun `whitelisting apps from showing stops them from being shown`() {
    coEvery { whitelistRepo.canShow("com.tymwitko.recents") } returns false
    coEvery { whitelistRepo.canShow("org.fake.app") } returns true
    runTest {
      val apps = viewModel.getActiveAppsFiltered(
        "com.tymwitko.recents"
      )
      Thread.sleep(SLEEP)
      assertEquals(
        listOf("Fake App" to "org.fake.app"),
        viewModel.appList.value?.map { it.name to it.packageName }
      )
    }   
  }
}