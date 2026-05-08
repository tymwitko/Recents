package com.tymwitko.recents.common.dataclasses

import android.content.ComponentName
import androidx.compose.ui.graphics.ImageBitmap

open class App(
  open val name: String,
  open val packageName: String,
  open var icon: ImageBitmap?,
  open var lastTimeUsed: Long?
)

data class DumpApp(
  override val name: String,
  override val packageName: String,
  override var icon: ImageBitmap?,
  override var lastTimeUsed: Long?,
  val componentName: ComponentName,
  val isWorkApp: Boolean
): App(name, packageName, icon, lastTimeUsed) {
  override fun equals(other: Any?): Boolean = (other as? DumpApp)?.let { 
    packageName == it.packageName
  } ?: super.equals(other)
}
