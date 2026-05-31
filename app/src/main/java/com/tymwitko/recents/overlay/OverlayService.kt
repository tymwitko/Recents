package com.tymwitko.recents.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.scottyab.rootbeer.RootBeer
import com.tymwitko.recents.R
import com.tymwitko.recents.common.accessors.AppKiller
import com.tymwitko.recents.common.accessors.AppsAccessor
import com.tymwitko.recents.common.accessors.IntentSender
import com.tymwitko.recents.common.accessors.ShizukuManager
import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.common.dataclasses.DumpApp
import com.tymwitko.recents.common.ui.compost.RecentAppsTheme
import com.tymwitko.recents.recentapps.RecentAppsActivity
import com.tymwitko.recents.settings.SettingsHolder
import com.tymwitko.recents.settings.whitelist.db.WhitelistRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class OverlayService : Service() {

    private val appsAccessor: AppsAccessor by inject()
    private val appKiller: AppKiller by inject()
    private val intentSender: IntentSender by inject()
    private val shizukuManager: ShizukuManager by inject()
    private val rootBeer: RootBeer by inject()
    private val whitelistRepository: WhitelistRepository by inject()
    private val settingsHolder: SettingsHolder by inject()

    private lateinit var windowManager: WindowManager
    private lateinit var lifecycleOwner: OverlayLifecycleOwner
    private lateinit var fastRunningAppsAccessor: FastRunningAppsAccessor

    private var overlayView: ComposeView? = null
    private var shizukuListenerRegistered = false
    private var refreshJob: Job? = null
    private var loadJob: Job? = null

    private val appList = mutableStateListOf<App>()
    private val killAllowedByPackage = mutableStateMapOf<String, Boolean>()
    private val isLoading = mutableStateOf(true)
    private val statusText = mutableStateOf("Loading recent apps...")

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        lifecycleOwner = OverlayLifecycleOwner().also { it.start() }
        fastRunningAppsAccessor = FastRunningAppsAccessor(applicationContext)

        createNotificationChannel()
        startOverlayForeground()
        setupShizuku()

        loadApps(
            useCacheFirst = true,
            showLoading = appList.isEmpty(),
            refreshReason = "Loading recent apps..."
        )
        startWarmRefreshLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                OverlayModePrefs.setEnabled(this, false)
                hideOverlay()
                stopSelf()
            }

            ACTION_HIDE_OVERLAY -> {
                hideOverlay()
            }

            ACTION_SHOW_OVERLAY -> {
                if (!OverlayModePrefs.isEnabled(this)) {
                    OverlayModePrefs.setEnabled(this, true)
                }

                if (!OverlayPermissionHelper.canDrawOverlays(this)) {
                    Toast.makeText(
                        this,
                        "Draw over other apps permission is required.",
                        Toast.LENGTH_LONG
                    ).show()

                    OverlayModePrefs.setOverlayPermissionPrompted(this, false)
                    OverlayPermissionHelper.openOverlayPermissionSettings(this)

                    return START_STICKY
                }

                attachOverlayView()
                loadApps(
                    useCacheFirst = true,
                    showLoading = appList.isEmpty(),
                    refreshReason = "Loading recent apps..."
                )
            }

            ACTION_REFRESH -> {
                loadApps(
                    useCacheFirst = false,
                    showLoading = true,
                    refreshReason = "Refreshing active apps..."
                )
            }

            else -> {
                if (OverlayModePrefs.isEnabled(this)) {
                    loadApps(
                        useCacheFirst = true,
                        showLoading = appList.isEmpty(),
                        refreshReason = "Loading recent apps..."
                    )
                    startWarmRefreshLoop()
                } else {
                    stopSelf()
                }
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        hideOverlay()

        refreshJob?.cancel()
        refreshJob = null

        loadJob?.cancel()
        loadJob = null

        if (shizukuListenerRegistered) {
            runCatching {
                shizukuManager.shutdownShizuku()
            }

            shizukuListenerRegistered = false
        }

        serviceScope.cancel()

        if (::lifecycleOwner.isInitialized) {
            runCatching {
                lifecycleOwner.stop()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startOverlayForeground() {
        val notification = buildNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun setupShizuku() {
        runCatching {
            shizukuManager.setupPermissionListener(packageName) { _, result ->
                if (result == PackageManager.PERMISSION_GRANTED) {
                    loadApps(
                        useCacheFirst = false,
                        showLoading = false,
                        refreshReason = "Refreshing active apps..."
                    )
                }
            }

            shizukuListenerRegistered = true
            shizukuManager.requestShizukuPermission()
        }
    }

    private fun startWarmRefreshLoop() {
        if (refreshJob?.isActive == true) {
            return
        }

        refreshJob = serviceScope.launch {
            while (isActive) {
                if (!OverlayModePrefs.isEnabled(this@OverlayService)) {
                    hideOverlay()
                    stopSelf()
                    return@launch
                }

                loadApps(
                    useCacheFirst = true,
                    showLoading = false,
                    refreshReason = "Refreshing active apps..."
                )

                delay(getWarmRefreshIntervalMs())
            }
        }
    }

    private fun getWarmRefreshIntervalMs(): Long {
        return runCatching {
            OverlayRefreshPrefs.getRefreshSeconds(this).toLong() * 1000L
        }.getOrDefault(WARM_REFRESH_INTERVAL_MS)
    }

    private fun attachOverlayView() {
        if (overlayView != null) return

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        val view = ComposeView(this).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(lifecycleOwner)
            )

            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)

            setContent {
                RecentAppsTheme {
                    OverlayRecentsRoot(
                        apps = appList,
                        isLoading = isLoading.value,
                        status = statusText.value,
                        hasPrivileges = hasPrivileges(),
                        canKillApp = { app -> canKillApp(app) },
                        canKillAny = appList.any { app -> canKillApp(app) },
                        onClose = {
                            hideOverlay()
                        },
                        onOpenFullApp = {
                            openFullApp()
                        },
                        onRefresh = {
                            loadApps(
                                useCacheFirst = false,
                                showLoading = true,
                                refreshReason = "Refreshing active apps..."
                            )
                        },
                        onLaunchApp = { app ->
                            launchApp(app)
                        },
                        onKillApp = { app ->
                            killApp(app)
                        },
                        onKillAll = {
                            killAllApps()
                        }
                    )
                }
            }
        }

        overlayView = view

        runCatching {
            windowManager.addView(view, params)
        }.onFailure {
            overlayView = null

            Toast.makeText(
                this,
                "Overlay permission is not active. Please allow Draw over other apps for Recents.",
                Toast.LENGTH_LONG
            ).show()

            OverlayModePrefs.setOverlayPermissionPrompted(this, false)
            OverlayPermissionHelper.openOverlayPermissionSettings(this)
        }
    }

    private fun hideOverlay() {
        overlayView?.let { view ->
            runCatching {
                windowManager.removeView(view)
            }
        }

        overlayView = null
    }

    private fun loadApps(
        useCacheFirst: Boolean,
        showLoading: Boolean,
        refreshReason: String
    ) {
        loadJob?.cancel()
        loadJob = serviceScope.launch {
            val onlyRunningNow = settingsHolder.getOnlyRunning() && hasPrivileges()

            if (showLoading || appList.isEmpty()) {
                isLoading.value = true
                statusText.value = refreshReason
            }

            if (useCacheFirst && RecentAppsCache.hasApps()) {
                val cachedOverlayData = withContext(Dispatchers.IO) {
                    applyCurrentSettingsTo(RecentAppsCache.getApps())
                }

                updateDisplayedApps(cachedOverlayData)

                isLoading.value = false
                statusText.value = statusFor(
                    appCount = cachedOverlayData.apps.size,
                    fromCache = true,
                    fastActive = false
                )

                // For all-apps mode, a fresh cache is fine. For active-only mode,
                // continue to a strict live scan so stale active apps are removed.
                if (RecentAppsCache.isFresh() && !onlyRunningNow) {
                    return@launch
                }

                if (showLoading) {
                    isLoading.value = true
                    statusText.value = refreshReason
                }
            }

            val loadedOverlayData = withContext(Dispatchers.IO) {
                runCatching {
                    val onlyRunning = settingsHolder.getOnlyRunning() && hasPrivileges()

                    val fastScan = if (onlyRunning && fastRunningAppsAccessor.canUseShizuku()) {
                        fastRunningAppsAccessor.getActiveAppsStrict(excludePackageName = packageName)
                    } else {
                        FastRunningAppsAccessor.FastActiveScanResult(
                            success = false,
                            apps = emptyList()
                        )
                    }

                    val rawApps = if (onlyRunning && fastScan.success) {
                        fastScan.apps
                    } else {
                        appsAccessor
                            .getRecentApps(onlyRunning)
                            .toList()
                    }

                    val data = applyCurrentSettingsTo(rawApps)

                    if (onlyRunning && fastScan.success) {
                        data.copy(usedFastActiveScanner = true)
                    } else {
                        data
                    }
                }.getOrElse {
                    OverlayAppData(
                        apps = emptyList(),
                        killAllowedPackages = emptySet(),
                        usedFastActiveScanner = false
                    )
                }
            }

            RecentAppsCache.update(loadedOverlayData.apps)
            updateDisplayedApps(loadedOverlayData)

            isLoading.value = false
            statusText.value = statusFor(
                appCount = loadedOverlayData.apps.size,
                fromCache = false,
                fastActive = loadedOverlayData.usedFastActiveScanner
            )
        }
    }

    private suspend fun applyCurrentSettingsTo(rawApps: List<App>): OverlayAppData {
        val privileged = hasPrivileges()
        val onlyRunning = settingsHolder.getOnlyRunning() && privileged

        val filteredApps = rawApps
            .filter { app ->
                app.packageName != packageName &&
                        !appsAccessor.isLauncher(app.packageName) &&
                        whitelistRepository.canShow(app.packageName) &&
                        (!onlyRunning || app.isRunning)
            }
            .distinctByPackagePickBest()
            .sortedByDescending { app -> app.lastTimeUsed ?: 0L }

        val killAllowedPackages = if (privileged) {
            filteredApps
                .filter { app -> whitelistRepository.canKill(app.packageName) }
                .map { app -> app.packageName }
                .toSet()
        } else {
            emptySet()
        }

        return OverlayAppData(
            apps = filteredApps,
            killAllowedPackages = killAllowedPackages,
            usedFastActiveScanner = false
        )
    }

    private fun updateDisplayedApps(data: OverlayAppData) {
        appList.clear()
        appList.addAll(data.apps)

        killAllowedByPackage.clear()
        data.killAllowedPackages.forEach { packageName ->
            killAllowedByPackage[packageName] = true
        }
    }

    private fun statusFor(appCount: Int, fromCache: Boolean, fastActive: Boolean): String {
        val source = when {
            fastActive -> "strict active • "
            fromCache -> "cached • "
            else -> ""
        }

        val running = if (settingsHolder.getOnlyRunning() && hasPrivileges()) {
            "active only • "
        } else {
            ""
        }

        return if (hasPrivileges()) {
            "$appCount apps • ${source}${running}kill enabled"
        } else {
            "$appCount apps • ${source}switch only"
        }
    }

    private fun canKillApp(app: App): Boolean {
        return hasPrivileges() && killAllowedByPackage[app.packageName] == true
    }

    private fun launchApp(app: App) {
        val launched = intentSender.launchSelectedApp(app) { intent ->
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        if (launched) {
            hideOverlay()
        } else {
            Toast.makeText(
                this,
                "Could not launch ${app.name}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun killApp(app: App) {
        if (!canKillApp(app)) {
            Toast.makeText(
                this,
                "This app is protected by your Recents whitelist/settings.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        serviceScope.launch {
            val killed = withContext(Dispatchers.IO) {
                runCatching {
                    appKiller.killByPackageName(app.packageName)
                    true
                }.getOrDefault(false)
            }

            if (killed) {
                appList.removeAll { item ->
                    item.packageName == app.packageName
                }

                killAllowedByPackage.remove(app.packageName)
                RecentAppsCache.update(appList.toList())
                statusText.value = statusFor(
                    appList.size,
                    fromCache = false,
                    fastActive = false
                )
            } else {
                Toast.makeText(
                    this@OverlayService,
                    "Could not kill ${app.name}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun killAllApps() {
        if (!hasPrivileges()) {
            Toast.makeText(
                this,
                "Shizuku or root is required to kill apps.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val appsToKill = appList.toList().filter { app ->
            canKillApp(app)
        }

        if (appsToKill.isEmpty()) {
            Toast.makeText(
                this,
                "No killable apps. Check your Recents whitelist/settings.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        serviceScope.launch {
            isLoading.value = true
            statusText.value = "Killing apps..."

            val killedCount = withContext(Dispatchers.IO) {
                var count = 0

                appsToKill.forEach { app ->
                    val killed = runCatching {
                        appKiller.killByPackageName(app.packageName)
                        true
                    }.getOrDefault(false)

                    if (killed) count++
                }

                count
            }

            Toast.makeText(
                this@OverlayService,
                "Killed $killedCount apps",
                Toast.LENGTH_SHORT
            ).show()

            loadApps(
                useCacheFirst = false,
                showLoading = true,
                refreshReason = "Refreshing active apps..."
            )
        }
    }

    private fun openFullApp() {
        hideOverlay()

        startActivity(
            Intent(this, RecentAppsActivity::class.java).apply {
                putExtra(OverlayModePrefs.EXTRA_FORCE_FULL_APP, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        )
    }

    private fun hasPrivileges(): Boolean {
        return shizukuManager.isShizukuAllowed() || rootBeer.isRooted
    }

    private fun List<App>.distinctByPackagePickBest(): List<App> {
        return groupBy { app ->
            app.packageName
        }.map { entry ->
            entry.value
                .filter { app ->
                    app !is DumpApp
                }
                .takeIf { normalApps ->
                    normalApps.isNotEmpty()
                }
                ?.maxByOrNull { app ->
                    app.lastTimeUsed ?: 0L
                }
                ?: entry.value.first()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Recents Overlay",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Floating recent-apps overlay"
            setSound(null, null)
            enableVibration(false)
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val stopIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, OverlayService::class.java).apply {
                action = ACTION_STOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val openFullAppIntent = PendingIntent.getActivity(
            this,
            1,
            Intent(this, RecentAppsActivity::class.java).apply {
                putExtra(OverlayModePrefs.EXTRA_FORCE_FULL_APP, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Recents overlay active")
            .setContentText("Background app list is warm")
            .setContentIntent(openFullAppIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Disable overlay",
                stopIntent
            )
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "recents_overlay"
        private const val NOTIFICATION_ID = 1337
        private const val WARM_REFRESH_INTERVAL_MS = 10_000L

        const val ACTION_STOP = "com.tymwitko.recents.overlay.STOP"
        const val ACTION_HIDE_OVERLAY = "com.tymwitko.recents.overlay.HIDE"
        const val ACTION_SHOW_OVERLAY = "com.tymwitko.recents.overlay.SHOW"
        const val ACTION_REFRESH = "com.tymwitko.recents.overlay.REFRESH"

        fun start(context: Context) {
            val intent = Intent(context, OverlayService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun show(context: Context) {
            val intent = Intent(context, OverlayService::class.java).apply {
                action = ACTION_SHOW_OVERLAY
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun refresh(context: Context) {
            val intent = Intent(context, OverlayService::class.java).apply {
                action = ACTION_REFRESH
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun hide(context: Context) {
            context.startService(
                Intent(context, OverlayService::class.java).apply {
                    action = ACTION_HIDE_OVERLAY
                }
            )
        }

        fun stop(context: Context) {
            context.startService(
                Intent(context, OverlayService::class.java).apply {
                    action = ACTION_STOP
                }
            )
        }
    }
}

private data class OverlayAppData(
    val apps: List<App>,
    val killAllowedPackages: Set<String>,
    val usedFastActiveScanner: Boolean
)

class OverlayLifecycleOwner :
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val viewModelStore: ViewModelStore
        get() = store

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    fun start() {
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    fun stop() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        store.clear()
    }
}
