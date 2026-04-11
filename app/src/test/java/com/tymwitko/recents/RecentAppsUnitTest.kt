package com.tymwitko.recents

import android.content.pm.PackageInfo
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.tymwitko.recents.common.accessors.AppKiller
import com.tymwitko.recents.common.accessors.AppsAccessor
import com.tymwitko.recents.common.accessors.IconAccessor
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.recentapps.RecentAppsViewModel
import com.tymwitko.recents.whitelist.WhitelistSettings
import com.tymwitko.recents.whitelist.db.PackageSettings
import com.tymwitko.recents.whitelist.db.WhitelistRepository
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

class RecentAppsUnitTest {
  private val whitelistRepo: WhitelistRepository = mockk<WhitelistRepository>()
  private val appsAccessor: AppsAccessor = mockk<AppsAccessor>(relaxed = true)
  private val iconAccessor: IconAccessor = mockk<IconAccessor>(relaxed = true)
  private val appKiller: AppKiller = mockk<AppKiller>(relaxed = true)
  private val shizukuManager: ShizukuManager = mockk<ShizukuManager>(relaxed = true)
  @get:Rule
  var rule: TestRule = InstantTaskExecutorRule()
  @OptIn(ExperimentalCoroutinesApi::class)
  val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

  val viewModel = RecentAppsViewModel(
    appsAccessor,
    appKiller,
    iconAccessor,
    mockk(),
    mockk(),
    whitelistRepo,
    shizukuManager
  )

  @Before
  fun `prepare tests`() {

    every {
      appsAccessor.getRecentAppsFormatted(any())
    } returns listOf("com.tymwitko.recents", "org.fake.app")
    every { appsAccessor.isLauncher(any()) } returns false
    coEvery { whitelistRepo.getEntry(any()) } returns PackageSettings(
      "com.tymwitko.recents",
      true,
      true,
      true
    )
    every { appsAccessor.getAppName("com.tymwitko.recents") } returns "Recents"
    every { appsAccessor.getAppName("org.fake.app") } returns "Fake App"
  }
  
  @Test
  fun `when getEntry called it should get settings`() {
    runTest {
      val apps = viewModel.getActiveApps("com.tymwitko.recents",
      null
      )
      coVerify { 
        whitelistRepo.getEntry("com.tymwitko.recents")
      }
    }
  }
  
  @Test
  fun `getting all apps should return a list of apps`() {
    runTest {
      val apps = viewModel.getActiveApps("com.tymwitko.recents",
        null
      )
      assertEquals(
        listOf(
          "com.tymwitko.recents" to "Recents",
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
      val apps = viewModel.getActiveApps("com.tymwitko.recents",
        null
      )
      Thread.sleep(3000)
      val settings = viewModel.getSettingsForApp("com.tymwitko.recents")?.value
      assertEquals(WhitelistSettings(true, true, true), settings)
    }
  }
  
  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `killing apps should call appKiller`() {
    every { appsAccessor.getRecentsAsPackageInfos(any()) } returns listOf(
      PackageInfo().apply { 
        packageName = "com.tymwitko.recents"
      },
      PackageInfo().apply { 
        packageName = "org.fake.app"
      }
    )
    Dispatchers.setMain(testDispatcher)
    runTest {
      val apps = viewModel.getActiveApps("com.tymwitko.recents",
        null
      )
      Thread.sleep(3000)
      viewModel.killEmAll("com.tymwitko.recents") {}
      coVerify(exactly = 2) { appKiller.killByPackageInfo(any()) }
    }
  }

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
    runTest {
      val apps = viewModel.getActiveApps("com.tymwitko.recents",
        null
      )
      Thread.sleep(3000)
      assertEquals(
        WhitelistSettings(true, false, false),
        viewModel.getSettingsForApp("com.tymwitko.recents")?.value
      )
    }
  }

  @Test
  fun `whitelisting apps from showing stops them from being shown`() {
    coEvery { whitelistRepo.canShow("com.tymwitko.recents") } returns false
    coEvery { whitelistRepo.canShow("org.fake.app") } returns true
    runTest {
      val apps = viewModel.getActiveAppsFiltered(
        "com.tymwitko.recents",
        null
      )
      Thread.sleep(3000)
      assertEquals(
        listOf("Fake App" to "org.fake.app"),
        viewModel.appList.value?.map { it.name to it.packageName }
      )
    }   
  }
}