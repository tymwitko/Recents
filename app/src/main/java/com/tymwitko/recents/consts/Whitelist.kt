package com.tymwitko.recents.consts

object Whitelist{
    val doNotKill = listOf(
        "io.heckel.ntfy", //NTFY notification service
        "dummydomain.yetanothercallblocker", // Yet Another Call Blocker
        "com.chen.deskclock" // Alarm Clock
    )
    val doNotLaunch = listOf(
        "net.blumia.pineapple.lockscreen.oss" // Pineapple LockScreen
    )

    fun isWhitelistedAgainstKilling(packageName: String) = doNotKill.contains(packageName)
    fun isWhitelistedAgainstLaunching(packageName: String) = doNotLaunch.contains(packageName)
}

