package com.iknowmuch.devicemanager.log

import android.content.Context
import com.iknowmuch.devicemanager.log.FileUtils.getLogDir
import me.pqpo.librarylog4a.LogData
import me.pqpo.librarylog4a.appender.AndroidAppender
import me.pqpo.librarylog4a.appender.FileAppender
import me.pqpo.librarylog4a.formatter.DateFileFormatter
import com.iknowmuch.devicemanager.log.LogInit
import me.pqpo.librarylog4a.Level
import me.pqpo.librarylog4a.logger.AppenderLogger
import me.pqpo.librarylog4a.Log4a
import me.pqpo.librarylog4a.interceptor.Interceptor
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by pqpo on 2017/11/24.
 */
object LogInit {
    const val BUFFER_SIZE = 1024 * 400 //400k
    fun init(context: Context?) {
        val level = Level.DEBUG
        val wrapInterceptor = Interceptor { logData ->
            logData.tag = "Log4a-" + logData.tag+" "
            true
        }
        val androidAppender = AndroidAppender.Builder()
            .setLevel(level)
            .addInterceptor(wrapInterceptor)
            .create()
        val log = getLogDir(context!!)
        val buffer_path = log.absolutePath + File.separator + ".logCache"
        val time = SimpleDateFormat("yyyy_MM_dd", Locale.getDefault()).format(Date())
        val log_path = log.absolutePath + File.separator + time + ".txt"
        val fileAppender = FileAppender.Builder(context)
            .setLogFilePath(log_path)
            .setLevel(level)
            .addInterceptor(wrapInterceptor)
            .setBufferFilePath(buffer_path)
            .setFormatter(DateFileFormatter())
            .setCompress(false)
            .setBufferSize(BUFFER_SIZE)
            .create()
        val logger = AppenderLogger.Builder()
            .addAppender(androidAppender)
            .addAppender(fileAppender)
            .create()
        Log4a.setLogger(logger)
    }
}