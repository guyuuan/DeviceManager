package com.iknowmuch.devicemanager.log.append

import android.util.Log
import me.pqpo.librarylog4a.appender.AbsAppender
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.LinkedBlockingQueue

class NoBufferInThreadFileAppender(private val logFile: File) : AbsAppender() {
    private val TAG = "NoBufferInThreadFileAppender"
    private val queue: LinkedBlockingQueue<String>
    private val witterThread: Thread
    private var isRunning = true
    override fun doAppend(logLevel: Int, tag: String, msg: String) {
        try {
            queue.put(logLevel.toString() + tag + msg)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun doAppendInner(log: String?) {
        var outputStream: OutputStream? = null
        try {
            outputStream = FileOutputStream(logFile)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        if (outputStream == null || log == null) {
            return
        }
        try {
            outputStream.write(log.toByteArray())
            outputStream.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun release() {
        super.release()
        isRunning = false
        witterThread.interrupt()
    }

    init {
        if (!logFile.exists()) {
            try {
                logFile.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        queue = LinkedBlockingQueue()
        witterThread = Thread {
            while (isRunning) {
                try {
                    val log = queue.take()
                    doAppendInner(log)
                    Log.d(TAG, log)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
        witterThread.start()
    }
}