package com.tymwitko.recents.accessors

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import org.koin.core.component.KoinComponent

class IconAccessor: KoinComponent {
    fun getAppIcon(context: Context, packageName: String): Drawable? =
        try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
}