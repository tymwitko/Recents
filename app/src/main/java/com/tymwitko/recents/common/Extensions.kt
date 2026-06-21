package com.tymwitko.recents.common

import com.tymwitko.recents.common.dataclasses.App
import com.tymwitko.recents.common.dataclasses.DumpApp

fun List<App>.distinctByNamePickApp(): List<App> =
  groupBy { it.getId() }
    .map {
      it.value.filter { app -> app as? DumpApp == null }
        .takeIf { it.isNotEmpty() }
        ?.maxByOrNull { it.lastTimeUsed ?: 0L } ?: it.value.first()
    }
  
