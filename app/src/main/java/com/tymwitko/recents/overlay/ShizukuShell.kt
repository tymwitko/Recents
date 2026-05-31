package com.tymwitko.recents.overlay

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.util.concurrent.TimeUnit

object ShizukuShell {

    fun isAvailable(): Boolean {
        return runCatching {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        }.getOrDefault(false)
    }

    fun exec(command: String, timeoutSeconds: Long = 3): String {
        if (!isAvailable()) {
            return ""
        }

        return runCatching {
            val newProcessMethod = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            ).apply {
                isAccessible = true
            }

            val process = newProcessMethod.invoke(
                null,
                arrayOf("sh", "-c", command),
                null,
                null
            ) as Process

            val output = process.inputStream.bufferedReader().readText()

            // Drain stderr so the process cannot block if shell prints warnings.
            process.errorStream.bufferedReader().readText()

            if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                process.destroy()
            }

            output
        }.getOrDefault("")
    }
}
