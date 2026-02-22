package com.tymwitko.recents.accessors

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.map

val SYSTEM_APPS_VISIBILITY_KEY = booleanPreferencesKey("show_system_apps")
val Context.systemAppsDataStore by preferencesDataStore("system_apps_visibility_prefs")

class SystemAppsVisibilityManager(private val context: Context) {
  suspend fun toggleVisibility(newValue: Boolean) {
    context.systemAppsDataStore.edit {
      it[SYSTEM_APPS_VISIBILITY_KEY] = newValue
    }
  }

  fun shouldShowSystemApps() = context.systemAppsDataStore.data.map {
    it[SYSTEM_APPS_VISIBILITY_KEY] ?: true
  }.asLiveData().value
}