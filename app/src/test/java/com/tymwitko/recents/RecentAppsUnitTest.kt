package com.tymwitko.recents

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.tymwitko.recents.common.accessors.AppKiller
import com.tymwitko.recents.common.accessors.AppsAccessor
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.recentapps.RecentAppsViewModel
import com.tymwitko.recents.settings.SettingsHolder
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

internal const val SLEEP = 1000L

class RecentAppsUnitTest {
  private val whitelistRepo: WhitelistRepository = mockk<WhitelistRepository>()
  private val appsAccessor: AppsAccessor = mockk<AppsAccessor>(relaxed = true)
  private val appKiller: AppKiller = mockk<AppKiller>(relaxed = true)
  private val shizukuManager: ShizukuManager = mockk<ShizukuManager>(relaxed = true)
  private val settingsHolder: SettingsHolder = mockk<SettingsHolder>(relaxed = true)
  @get:Rule
  var rule: TestRule = InstantTaskExecutorRule()
  @OptIn(ExperimentalCoroutinesApi::class)
  val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

  val viewModel = RecentAppsViewModel(
    appsAccessor,
    appKiller,
    mockk(),
    mockk(),
    whitelistRepo,
    shizukuManager,
    settingsHolder
  )

  @Before
  fun `prepare tests`() {

    coEvery {
      appsAccessor.getRecentApps(any())
    } returns flowOf(
      App("Recents","com.tymwitko.recents", null, 0L, true),
      App("Fake App","org.fake.app", null, 0L, true)
    )
    every { appsAccessor.isLauncher(any()) } returns false
    coEvery { whitelistRepo.getEntry(any()) } returns PackageSettings(
      "com.tymwitko.recents",
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
      val apps = viewModel.getApps("com.tymwitko.recents", false)
      coVerify { 
        whitelistRepo.getEntry("org.fake.app")
      }
    }
  }
  
  @Test
  fun `getting all apps should return a list of apps`() {
    runTest {
      val apps = viewModel.getApps("com.tymwitko.recents", false)
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
      val apps = viewModel.getApps("com.tymwitko.recents", false)
      Thread.sleep(SLEEP)
      val settings = viewModel.getSettingsForApp("org.fake.app")?.value
      assertEquals(WhitelistSettingsData(canLaunch = true, canKill = true, canShow = true), settings)
    }
  }
  
  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `killing apps should call appKiller`() {
    Dispatchers.setMain(testDispatcher)
    runTest {
      val apps = viewModel.getApps("com.tymwitko.recents", false)
      Thread.sleep(SLEEP)
      viewModel.killEmAll("com.tymwitko.recents") {}
      coVerify(exactly = 1) { appKiller.killByPackageName(any()) }
    }
  }

  @Test
  fun `whitelisting apps updates settings`() {
    coEvery { whitelistRepo.getEntry("com.tymwitko.recents") } returns PackageSettings(
      "com.tymwitko.recents",
      canLaunch = true, canKill = false, canShow = false
    )
    coEvery { whitelistRepo.getEntry("org.fake.app") } returns PackageSettings(
      "org.fake.app",
      canLaunch = true, canKill = false, canShow = true
    )
    runTest {
      val apps = viewModel.getApps("com.tymwitko.recents", false)
      Thread.sleep(SLEEP)
      assertEquals(
        WhitelistSettingsData(canLaunch = true, canKill = false, canShow = true),
        viewModel.getSettingsForApp("org.fake.app")?.value
      )
    }
  }

  @Test
  fun `whitelisting apps from showing stops them from being shown`() {
    coEvery { whitelistRepo.canShow("com.tymwitko.recents") } returns false
    coEvery { whitelistRepo.canShow("org.fake.app") } returns true
    runTest {
      viewModel.fetchApps("com.tymwitko.recents")
      Thread.sleep(SLEEP)
      assertEquals(
        listOf("Fake App" to "org.fake.app"),
        viewModel.appList.value?.map { it.name to it.packageName }
      )
    }   
  }
  
  @Test
  fun `only running setting should filter apps by running`() {
    coEvery {
      appsAccessor.getRecentApps(any())
    } returns flowOf(
      App("Recents","com.tymwitko.recents", null, 0L, true),
      App("Github Copilot","ai.is.theft", null, 0L, false),
      App("Fake App","org.fake.app", null, 0L, true)
    )
    runTest {
      val apps = viewModel.getApps("com.tymwitko.recents", true)
      println(apps.map { it.packageName })
      assertEquals(
        listOf("org.fake.app"),
        apps.map { it.packageName }
      )
    }
  }
}