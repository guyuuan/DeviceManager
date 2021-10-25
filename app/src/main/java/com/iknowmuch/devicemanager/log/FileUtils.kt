package com.iknowmuch.devicemanager.log

import android.content.Context
import java.io.File

object FileUtils {
    @JvmStatic
    fun getLogDir(context: Context): File {
        var log = context.getExternalFilesDir("logs")
        if (log == null) {
            log = File(context.filesDir, "logs")
        }
        if (!log.exists()) {
            log.mkdir()
        }
        if (getLogSize(log) > 512) {
            deleteLog(log)
        }
        return log
    }

    private fun deleteLog(log: File) {
        if (log.isDirectory) {
            val logs = log.listFiles()
            repeat(logs.size / 4) {
                logs[it].delete()
            }
        }
    }

    private fun getLogSize(log: File): Long {
        var size = 0L
        if (log.isDirectory) {
            val logs = log.listFiles()
            for (file in logs) {
                size += file.length()
            }
        }
        return size / 1024 / 1024
    }
}