package com.iknowmuch.devicemanager.log.append

import me.pqpo.librarylog4a.appender.AbsAppender
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class NoBufferFileAppender(private val logFile: File) : AbsAppender() {
    private var outputStream: OutputStream? = null
    private fun openFileOutputStream() {
        if (!logFile.exists()) {
            try {
                if (!logFile.createNewFile()) {
                    return
                }
                outputStream = FileOutputStream(logFile)
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