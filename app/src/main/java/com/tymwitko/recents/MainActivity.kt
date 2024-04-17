package com.tymwitko.recents

import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        getRecentApps(this).forEachIndexed { ind, app ->
//            Log.d("APP", "App $ind: $app")
//        }
        launchLastApp(this)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_main)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
    }

    fun getRecentApps(context: Context): List<String> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 1000 * 60 * 60 * 24 // for last 24 hours stats
        val usageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, beginTime, endTime)

        return usageStats.filter { it.lastTimeUsed > beginTime }
            .sortedBy { it.lastTimeUsed }
            .map { it.packageName }
//            .map { "${it.packageName} time: ${(Instant.ofEpochMilli(it.lastTimeUsed).atZone(ZoneId.systemDefault()))}, timeForeground: ${Instant.ofEpochMilli(it.lastTimeForegroundServiceUsed).atZone(ZoneId.systemDefault())}" }
    }

    private fun launchLastApp(context: Context) {
        val lastApps = getRecentApps(context)
        Log.d("TAG", "Dropping last 2 apps: ${lastApps.last()} and ${lastApps[lastApps.size - 2]}")
        lastApps
            .filter { it != "com.tymwitko.recents" }
            .dropLast(1)
            .filter { it != "com.benny.openlauncher" }
            .reversed().forEach {
                Log.d("TAG", "last app is $it")
                val launchIntent = packageManager.getLaunchIntentForPackage(it)
                launchIntent?.let {
                    Log.d("TAG","Launching app $it")
                    startActivity(launchIntent)
                    finish()
                    return
                }
                Log.d("TAG", "Launching app failed, possibly it lacks an Activity")
            }
    }
}