package com.tymwitko.recents.consts

object Whitelist{
    val doNotKill = listOf(
        "io.heckel.ntfy", //NTFY notification service
        "dummydomain.yetanothercallblocker", // Yet Another Call Blocker
        "com.chen.deskclock" // Alarm Clock
    )
}

fun isWhitelisted(packageName: String) = Whitelist.doNotKill.contains(packageName)