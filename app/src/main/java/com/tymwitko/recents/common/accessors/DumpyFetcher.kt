package com.tymwitko.recents.common.accessors

import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class DumpyFetcher {
  val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).apply {
    timeZone = TimeZone.getDefault()
  }

  fun runCmd(): BufferedReader {
    val proc = Runtime.getRuntime().exec(arrayOf("sh", "-c", "dumpsys usagestats"))
    return BufferedReader(InputStreamReader(proc.inputStream))
  }

  fun getLastUsesViaDumpsys(): HashMap<String, Long> {
    val reader = runCmd()
    val results = hashMapOf<String, Long>()
    var currentPair: Pair<String, Long>? = null

    val reSummary = Regex(
      """package=(\S+)\s+.*lastTimeUsed="([^"]+)".*totalTimeUsed="([^"]+)".*lastTimeComponentUsed="([^"]+)"""
    )
    val reSummaryAlt = Regex(
      """package=(\S+)\s+.*lastTimeUsed="([^"]+)"(?:.*lastTimeComponentUsed="([^"]+)")?"""
    )

    reader.useLines { lines ->
      lines.forEach { line ->
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
}
