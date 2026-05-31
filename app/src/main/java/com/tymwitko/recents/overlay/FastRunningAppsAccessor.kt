package com.tymwitko.recents.overlay

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.tymwitko.recents.common.dataclasses.App
import java.util.concurrent.ConcurrentHashMap

class FastRunningAppsAccessor(
    context: Context
) {
    private val appContext = context.applicationContext
    private val packageManager = appContext.packageManager
    private val metaCache = ConcurrentHashMap<String, AppMeta>()

    fun canUseShizuku(): Boolean {
        return ShizukuShell.isAvailable()
    }

    /**
     * Strict active scan.
     *
     * This intentionally does NOT treat every package from `ps` as an active app.
     * `ps` is used only to confirm that a package from the user-facing activity/task
     * stack still has a live process.
     */
    fun getActiveAppsStrict(excludePackageName: String): FastActiveScanResult {
        if (!canUseShizuku()) {
            return FastActiveScanResult(success = false, apps = emptyList())
        }

        val shellOutput = ShizukuShell.exec(
            """
            ps -A -o NAME 2>/dev/null || ps -A 2>/dev/null
            echo __RECENTS_START__
            dumpsys activity recents 2>/dev/null
            echo __ACTIVITIES_START__
            dumpsys activity activities 2>/dev/null
            """.trimIndent(),
            timeoutSeconds = 3
        )

        if (shellOutput.isBlank()) {
            return FastActiveScanResult(success = false, apps = emptyList())
        }

        val lines = shellOutput.lines()
        val recentsStart = lines.indexOf("__RECENTS_START__").takeIf { it >= 0 } ?: return FastActiveScanResult(false, emptyList())
        val activitiesStart = lines.indexOf("__ACTIVITIES_START__").takeIf { it >= 0 } ?: return FastActiveScanResult(false, emptyList())

        val psLines = lines.take(recentsStart)
        val recentsLines = lines.subList(
            recentsStart + 1,
            activitiesStart.coerceAtLeast(recentsStart + 1)
        )
        val activityLines = lines.drop(activitiesStart + 1)

        val runningPackages = parseProcessPackages(psLines).toSet()
        val orderedPackages = LinkedHashMap<String, Long>()
        val now = System.currentTimeMillis()

        // Foreground/focused/resumed packages are always first.
        parseForegroundPackages(activityLines).forEach { packageName ->
            orderedPackages.putIfAbsent(packageName, now + 10_000L)
        }

        // Recent task packages are next, but only if the process is still live.
        parseRecentPackages(recentsLines)
            .entries
            .sortedByDescending { it.value }
            .forEach { entry ->
                orderedPackages.putIfAbsent(entry.key, entry.value)
            }

        val apps = orderedPackages
            .filterKeys { packageName ->
                packageName != excludePackageName &&
                        isLaunchablePackage(packageName) &&
                        // If a package is foreground/focused, keep it even if ps output is weird.
                        (runningPackages.contains(packageName) || orderedPackages[packageName] == now + 10_000L)
            }
            .mapNotNull { (packageName, lastUsed) ->
                toApp(packageName, lastUsed)
            }
            .distinctBy { it.packageName }

        return FastActiveScanResult(success = true, apps = apps)
    }

    /**
     * Kept for compatibility with earlier OverlayService versions.
     */
    fun getRunningApps(excludePackageName: String): List<App> {
        return getActiveAppsStrict(excludePackageName).apps
    }

    private fun parseForegroundPackages(lines: List<String>): List<String> {
        val importantLines = lines.filter { line ->
            line.contains("topResumedActivity") ||
                    line.contains("mResumedActivity") ||
                    line.contains("ResumedActivity") ||
                    line.contains("mFocusedApp") ||
                    line.contains("mCurrentFocus")
        }

        return importantLines
            .mapNotNull { line -> extractPackageFromComponentLine(line) }
            .distinct()
    }

    private fun parseRecentPackages(lines: List<String>): Map<String, Long> {
        val results = linkedMapOf<String, Long>()

        val idLineRegex = Regex("""\bid=(\d+)\s+userId=(\d+).*hasTask=(\S+).*lastActiveTime=(\d+)""")
        val activityRegexes = listOf(
            Regex("""\brealActivity=\{([^/}\s]+)/"""),
            Regex("""\brealActivity=ComponentInfo\{([^/}\s]+)/"""),
            Regex("""\bbaseActivity=ComponentInfo\{([^/}\s]+)/"""),
            Regex("""\btopActivity=ComponentInfo\{([^/}\s]+)/"""),
            Regex("""\bintent=\{[^}]*cmp=([^/}\s]+)/""")
        )

        var hasTask = false
        var lastActiveEpochMs = 0L

        lines.forEach { line ->
            idLineRegex.find(line)?.let { match ->
                hasTask = match.groupValues[3].toBooleanStrictOrNull() ?: false
                lastActiveEpochMs = elapsedToEpochMs(
                    match.groupValues[4].toLongOrNull() ?: 0L
                )
                return@forEach
            }

            val packageName = activityRegexes
                .firstNotNullOfOrNull { regex ->
                    regex.find(line)?.groupValues?.getOrNull(1)
                }
                ?: extractPackageFromComponentLine(line)

            if (packageName != null && hasTask) {
                results[packageName] = lastActiveEpochMs
            }
        }

        return results
    }

    private fun parseProcessPackages(lines: List<String>): List<String> {
        return lines
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filterNot { it.equals("NAME", ignoreCase = true) || it.startsWith("USER ") }
            .mapNotNull { line ->
                val rawName = line.split(Regex("""\s+""")).lastOrNull()?.trim().orEmpty()
                rawName
                    .substringBefore(":")
                    .takeIf { looksLikePackageName(it) }
            }
            .distinct()
            .toList()
    }

    private fun extractPackageFromComponentLine(line: String): String? {
        val componentRegexes = listOf(
            Regex("""\bu\d+\s+([a-zA-Z][\w]*(?:\.[\w]+)+)/"""),
            Regex("""\b([a-zA-Z][\w]*(?:\.[\w]+)+)/[A-Za-z0-9_.$]+"""),
            Regex("""\bpackageName=([a-zA-Z][\w]*(?:\.[\w]+)+)"""),
            Regex("""\bcmp=([a-zA-Z][\w]*(?:\.[\w]+)+)/""")
        )

        return componentRegexes
            .firstNotNullOfOrNull { regex ->
                regex.find(line)?.groupValues?.getOrNull(1)
            }
            ?.takeIf { looksLikePackageName(it) }
    }

    private fun looksLikePackageName(value: String): Boolean {
        return PACKAGE_NAME_REGEX.matches(value)
    }

    private fun isLaunchablePackage(packageName: String): Boolean {
        return runCatching {
            packageManager.getLaunchIntentForPackage(packageName) != null &&
                    getApplicationInfo(packageName) != null
        }.getOrDefault(false)
    }

    private fun toApp(packageName: String, lastUsed: Long): App? {
        val meta = getMeta(packageName) ?: return null

        return App(
            name = meta.label,
            packageName = packageName,
            icon = meta.icon,
            lastTimeUsed = lastUsed.takeIf { it > 0L },
            isRunning = true
        )
    }

    private fun getMeta(packageName: String): AppMeta? {
        metaCache[packageName]?.let {
            return it
        }

        val appInfo = getApplicationInfo(packageName) ?: return null

        val label = runCatching {
            packageManager.getApplicationLabel(appInfo).toString()
        }.getOrDefault(packageName)

        val icon = runCatching {
            packageManager.getApplicationIcon(packageName)
                .toBitmap()
                .asImageBitmap()
        }.getOrNull()

        return AppMeta(label, icon).also {
            metaCache[packageName] = it
        }
    }

    private fun getApplicationInfo(packageName: String): ApplicationInfo? {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.ApplicationInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getApplicationInfo(packageName, 0)
            }
        }.getOrNull()
    }

    private fun elapsedToEpochMs(elapsedMs: Long): Long {
        val nowEpoch = System.currentTimeMillis()
        val elapsedNow = SystemClock.elapsedRealtime()
        val bootEpoch = nowEpoch - elapsedNow
        return bootEpoch + elapsedMs
    }

    data class FastActiveScanResult(
        val success: Boolean,
        val apps: List<App>
    )

    private data class AppMeta(
        val label: String,
        val icon: ImageBitmap?
    )

    companion object {
        private val PACKAGE_NAME_REGEX = Regex("""[a-zA-Z][\w]*(?:\.[\w]+)+""")
    }
}
