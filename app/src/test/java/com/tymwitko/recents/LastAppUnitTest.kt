package com.tymwitko.recents

import android.content.pm.PackageManager
import com.tymwitko.recents.common.accessors.AppsAccessor
import com.tymwitko.recents.common.accessors.IntentSender
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.lastapp.LastAppViewModel
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
  private val intentSender = IntentSender(pm, mockk())
  private val shizukuManager = mockk<ShizukuManager>(relaxed = true)

  val viewModel = LastAppViewModel(
    intentSender,
    appsAccessor,
    mockk(),
    shizukuManager
  )

  @Before
  fun `prepare tests`() {

    coEvery {
      appsAccessor.getRecentApps(any())
    } returns flowOf(
      App("Coming soon","com.tymwitko.differentapp", null, 0L, true),
      App("Fake App","org.fake.app", null, 0L, true),
      App("Github Copilot", "ai.is.theft", null, 0L, true)
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
      viewModel.launchLastApp({}, "com.tymwitko.recents")
      coVerify { intentSender.launchSelectedApp(App("Fake", "org.fake.app", null, 0L, true)) {} }
    }
  }
  
  @Test
  fun `whitelisting from launching should skip launching that app`() {
    coEvery { appsAccessor.shouldLaunch(any()) } returns true
    coEvery { appsAccessor.shouldLaunch("org.fake.app") } returns false
    runTest {
      viewModel.launchLastApp({}, "com.tymwitko.recents")
      coVerify { intentSender.launchSelectedApp(App("Github Copilot", "ai.is.theft", null, 0L, true)) {} }
    }   
  }
  
  @Test
  fun `launchers should not get launched`() {
    coEvery { appsAccessor.shouldLaunch("com.tymwitko.differentapp") } returns true
    coEvery { appsAccessor.shouldLaunch("ai.is.theft") } returns true
    coEvery { appsAccessor.isLauncher("org.fake.app") } returns true
    runTest {
      viewModel.launchLastApp({}, "com.tymwitko.recents")
      coVerify { intentSender.launchSelectedApp(App("Github Copilot", "ai.is.theft", null, 0L, true)) {} }
    }
  }
}