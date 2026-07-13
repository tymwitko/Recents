package com.tymwitko.recents.recentapps.quicksettings

enum class WhitelistSettingType {
  LAUNCH, KILL, SHOW;
  
  companion object {
    fun fromBoolean(
      canLaunch: Boolean? = null,
      canKill: Boolean? = null,
      canShow: Boolean? = null
    ) = when {
      canLaunch != null && canKill == null && canShow == null -> LAUNCH
      canLaunch == null && canKill != null && canShow == null -> KILL
      canLaunch == null && canKill == null && canShow != null -> SHOW
      else -> throw IllegalArgumentException("Only one value can be non-null!")
    }
  }
}