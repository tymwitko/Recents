package com.tymwitko.recents

import android.content.pm.PackageManager
import com.tymwitko.recents.common.accessors.AppsAccessor
import com.tymwitko.recents.common.accessors.IntentSender
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.lastapp.LastAppViewModel
import com.tymwitko.recents.settings.SettingsHolder
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class LastAppUnitTest {
  private val appsAccessor = mockk<AppsAccessor>(relaxed = true)
  private val pm = mockk<PackageManager>(relaxed = true)
  private val intentSender = IntentSender(pm, mockk(), mockk(relaxed = true))
  private val shizukuManager = mockk<ShizukuManager>(relaxed = true)
  private val settingsHolder: SettingsHolder = mockk<SettingsHolder>(relaxed = true)

  val viewModel = LastAppViewModel(
    intentSender,
    appsAccessor,
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
      App(
        name = "Coming soon",
        packageName = "com.tymwitko.differentapp",
        icon = null,
        lastTimeUsed = 2L,
        isRunning = true,
        isWorkApp = false
      ),
      App(
        name = "Fake App",
        packageName = "org.fake.app",
        icon = null,
        lastTimeUsed = 1L,
        isRunning = true,
        isWorkApp = false
      ),
      App(
        name = "Github Copilot",
        packageName = "ai.is.theft",
        icon = null,
        lastTimeUsed = 0L,
        isRunning = true,
        isWorkApp = false
      )
    )
    every { appsAccessor.isLauncher(any()) } returns false
    every { appsAccessor.getAppName("com.tymwitko.differentapp") } returns "Coming Soon"
    every { appsAccessor.getAppName("org.fake.app") } returns "Fake App"
    every { appsAccessor.getAppName("ai.is.theft") } returns "Github Copilot"
    every { shizukuManager.isShizukuAllowed() } returns true
  }

  @Test
  fun `last but one app should be launched`() {
    coEvery { appsAccessor.shouldLaunch(any()) } returns true
    runTest {
      viewModel.launchLastApp({ _, _ -> }, "com.tymwitko.recents")
      coVerify {
        intentSender.launchSelectedApp(
          App(
            name = "Fake App",
            packageName = "org.fake.app",
            icon = null,
            lastTimeUsed = 0L,
            isRunning = true,
            isWorkApp = false
          ),
          startActivity = { _, _ -> }
        )
      }
    }
  }

  @Test
  fun `whitelisting from launching should skip launching that app`() {
    coEvery { appsAccessor.shouldLaunch(any()) } returns true
    coEvery {
      appsAccessor.shouldLaunch(
        match {
          it.packageName == "org.fake.app"
        }
      )
    } returns false
    runTest {
      viewModel.launchLastApp({ _, _ -> }, "com.tymwitko.recents")
      coVerify {
        intentSender.launchSelectedApp(
          App(
            name = "Github Copilot",
            packageName = "ai.is.theft",
            icon = null,
            lastTimeUsed = 0L,
            isRunning = true,
            isWorkApp = false
          ),
          startActivity = { _, _ -> }
        )
      }
    }
  }

  @Test
  fun `launchers should not get launched`() {
    coEvery {
      appsAccessor.shouldLaunch(
        match { 
          it.packageName == "com.tymwitko.differentapp"
        }
      )
    } returns true
    coEvery {
      appsAccessor.shouldLaunch(
        match {
          it.packageName == "ai.is.theft"
        }
      )
    } returns true
    coEvery { appsAccessor.isLauncher("org.fake.app") } returns true
    runTest {
      viewModel.launchLastApp({ _, _ -> }, "com.tymwitko.recents")
      coVerify {
        intentSender.launchSelectedApp(
          App(
            name = "Github Copilot",
            packageName = "ai.is.theft",
            icon = null,
            lastTimeUsed = 0L,
            isRunning = true,
            isWorkApp = false
          ),
          startActivity = { _, _ -> }
        )
      }
    }
  }
}