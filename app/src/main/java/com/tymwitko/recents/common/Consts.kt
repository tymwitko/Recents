package com.tymwitko.recents.common

import com.tymwitko.recents.BuildConfig

const val DONATION_URL = "https://buymeacoffee.com/tymwitko"
const val ERROR_ISSUE_URL = "https://github.com/tymwitko/recents/issues/new?title=I encountered " +
  "the error screen!&body=**Describe how and when it happened**%0A%0A%0A**Paste the error log " +
  "below (it's in your clipboard)**%0A%0A%0A**App version**%0A${BuildConfig.VERSION_NAME}%0A%0A" +
  "**Enter your device model and OS version**%0A%0A"
const val REPORT_ISSUE_URL = "https://github.com/tymwitko/recents/issues/new?body=**Describe the " +
  "issue**%0A%0A%0A**Expected outcome**%0A%0A%0A**App version**%0A${BuildConfig.VERSION_NAME}" +
  "%0A%0A**Enter your device model and OS version**%0A%0A"
const val SHARED_PREFS_KEY = "com.tymwitko.recents.UI_PREFS"
const val FONT_SIZE_ALIAS = "FONT_SIZE"
const val ICON_SIZE_ALIAS = "ICON_SIZE"
const val ONLY_RUNNING_ALIAS = "ONLY_RUNNING"
const val SWIPE_TO_DEL_ALIAS = "SWIPE_TO_DELETE"
const val IS_RECENTS_DEFAULT_ALIAS = "DEFAULT_ACTIVITY"
const val IS_REVERSED_ORDER_ALIAS = "REVERSED_ORDER"
const val MARGIN_SIZE_ALIAS = "MARGIN_SIZE"
const val DEFAULT_FONT_SIZE = 12
const val DEFAULT_MARGIN_SIZE = 16
const val MILLIS_IN_DAY = 86400000
const val MILLIS_IN_HOUR = 3600000
const val WINDOWING_EXTRA = "android.intent.extra.WINDOWING_MODE"
const val LAUNCH_WINDOWING_EXTRA = "android.intent.extra.LAUNCH_WINDOWING_MODE"
const val SPLIT_MODE_SECONDARY = 4
const val FREEFORM_MODE = 5
const val WAIT_BETWEEN_SPLIT_MILLIS = 100
const val LAUNCH_WINDOWING_METHOD_NAME = "setLaunchWindowingMode"
