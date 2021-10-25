package com.iknowmuch.devicemanager.log.append

import me.pqpo.librarylog4a.appender.AbsAppender
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class BufferFileAppender(private val logFile: File, private val bufferSize: Int) : AbsAppender() {
    private var outputStream: BufferedOutputStream? = null
    private fun openFileOutputStream() {
        if (!logFile.exists()) {
            try {
                if (!logFile.createNewFile()) {
                    return
                }
                val os = FileOutputStream(logFile)
                outputStream = BufferedOutputStream(os, bufferSize)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun doAppend(logLevel: Int, tag: String, msg: String) {
        if (outputStream == null) {
            return
        }
        //        String logStr = String.format("%s/%s: %s\n", Level.getShortLevelName(logLevel), tag, msg);
        try {
            outputStream!!.write(msg.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun release() {
        super.release()
        if (outputStream != null) {
            try {
                outputStream!!.flush()
                outputStream!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    init {
        openFileOutputStream()
    }
}