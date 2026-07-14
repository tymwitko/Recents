package com.tymwitko.recents.common.accessors

import android.content.ComponentName
import android.util.Log
import com.tymwitko.recents.common.dataclasses.ActiveApp
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class DumpyFetcher {
  val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).apply {
    timeZone = TimeZone.getDefault()
  }

  fun runCmd(command: String): BufferedReader {
    val proc = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
    return BufferedReader(InputStreamReader(proc.inputStream))
  }

  fun getLastUsesViaDumpsys(): HashMap<String, Long> {
    val reader = runCmd("dumpsys usagestats")
    val results = hashMapOf<String, Long>()
    var currentPair: Pair<String, Long>? = null
    var dumpContent = ""

    val reSummary = Regex(
      """package=(\S+)\s+.*lastTimeUsed="([^"]+)".*totalTimeUsed="([^"]+)".*lastTimeComponentUsed="([^"]+)"""
    )
    val reSummaryAlt = Regex(
      """package=(\S+)\s+.*lastTimeUsed="([^"]+)"(?:.*lastTimeComponentUsed="([^"]+)")?"""
    )

    reader.useLines { lines ->
      lines.forEach { line ->
        dumpContent += line
        reSummary.find(line)?.let {
          currentPair = parseLine(
            it,
            format,
            currentPair?.first,
            results,
            currentPair?.second ?: 0L
          )
          return@forEach
        }

        reSummaryAlt.find(line)?.let {
          currentPair = parseLine(
            it,
            format,
            currentPair?.first,
            results,
            currentPair?.second ?: 0L
          )
          return@forEach
        }
      }
      currentPair?.let {
        results[it.first] = it.second
      }
    }
    if (results.isEmpty()) throw DumpFailedException(dumpContent)
    return results
  }
  
  fun getRunningPackages(): List<ActiveApp> {
    val reader = runCmd("dumpsys activity recents")
    val results = mutableListOf<ActiveApp>()

    val reRealActivity = Regex("""\brealActivity=\{([^/}]+)/""")
    val reIdLine = Regex("""\bid=(\d+)\s+userId=(\d+).*hasTask=(\S+)\s+.*lastActiveTime=(\d+)""")
    val reCompLine = Regex("""\bcmp=([^ }\n]+)""")
    val reHeader = Regex("""Recent tasks:.*""")

    var dumpContent = ""
    var hasHeader = false
    var hasTask = false
    var lastActiveEpochMs = Long.MAX_VALUE
    var userId = ""
    var componentName = ""

    reader.useLines { lines ->
      lines.forEach { line ->
        Log.i("DUMP", line)
        dumpContent += line
        reHeader.find(line)?.let { 
          hasHeader = true
        }
        reIdLine.find(line)?.let { m ->
          userId = m.groupValues[2]
          hasTask = m.groupValues[3].toBoolean()
          lastActiveEpochMs = elapsedToEpochMs(m.groupValues[4].toLong())
          return@forEach
        }
        reCompLine.find(line)?.let { 
          componentName = it.groupValues[1]
          return@forEach
        }
        reRealActivity.find(line)?.let { m ->
          val pendingPkg = m.groupValues[1]
          if (hasTask) results.add(
            ActiveApp(
              pendingPkg,
              true,
              lastActiveEpochMs,
              userId == "10",
              componentName.split("/").let { 
                ComponentName(it[0], it[1])
              }
            )
          )
          return@forEach
        }
      }
    }
    if (!hasHeader) throw DumpFailedException(dumpContent)
    return results
  }
  
  private fun parseLine(
    res: MatchResult,
    format: SimpleDateFormat,
    currentPkg: String?,
    results: HashMap<String, Long>,
    lastUsedEpoch: Long
  ): Pair<String, Long> {
    currentPkg?.let {
      results[it] = lastUsedEpoch
    }
    return (
      res.groupValues[1] to runCatching {
        format.parse(res.groupValues[3])?.time ?: 0L
      }.getOrDefault(0L)
    )
  }
  
  private fun elapsedToEpochMs(elapsedMs: Long): Long {
    val nowEpoch = System.currentTimeMillis()
    val elapsedNow = android.os.SystemClock.elapsedRealtime()
    val bootEpoch = nowEpoch - elapsedNow
    return bootEpoch + elapsedMs
  }
}
