package com.tymwitko.recents.overlay

import com.tymwitko.recents.common.dataclasses.App

object RecentAppsCache {

    private const val FRESH_FOR_MS = 10_000L

    private val lock = Any()
    private var cachedApps: List<App> = emptyList()
    private var lastUpdatedMs: Long = 0L

    fun update(apps: List<App>) {
        synchronized(lock) {
            cachedApps = apps
            lastUpdatedMs = System.currentTimeMillis()
        }
    }

    fun getApps(): List<App> {
        return synchronized(lock) {
            cachedApps
        }
    }

    fun hasApps(): Boolean {
        return synchronized(lock) {
            cachedApps.isNotEmpty()
        }
    }

    fun isFresh(): Boolean {
        return synchronized(lock) {
            cachedApps.isNotEmpty() &&
                    System.currentTimeMillis() - lastUpdatedMs <= FRESH_FOR_MS
        }
    }

    fun clear() {
        synchronized(lock) {
            cachedApps = emptyList()
            lastUpdatedMs = 0L
        }
    }
}
