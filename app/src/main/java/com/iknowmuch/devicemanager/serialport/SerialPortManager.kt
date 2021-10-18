package com.iknowmuch.devicemanager.serialport

import android.util.Log
import android_serialport_api.SerialPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 *@author: Chen
 *@createTime: 2021/9/16 13:25
 *@description:
 **/
private const val TAG = "SerialPortManager"

class SerialPortManager {
    //    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var serialPort: SerialPort? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
//    private var job: Job? = null
//    fun read(path: String = "/dev/ttyS2") {
//        try {
//            serialPort = SerialPort(File(path), 115200, 0)
//            job = coroutineScope.launch(Dispatchers.IO) {
//                serialPort?.inputStream?.use {
//                    while (true) {
//                        try {
//                            if (it.available() > 0) {
//                                delay(500)
//                                val byte = ByteArray(it.available())
//                                val len = it.read(byte).coerceAtLeast(0)
//                                val hex = byte.slice(0 until len).map { b -> b.toUByte() }
//                                    .joinToHexString()
//                                Log.d(TAG, "read: $hex")
//                            }
//                        } catch (e: Exception) {
//                            Log.e(TAG, "while: ", e)
//                        }
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            serialPort?.close()
//            Log.e(TAG, "run: ", e)
//        }
//    }

    fun init(path: String) {
        try {
            serialPort = SerialPort(File(path), 115200, 0)
            inputStream = serialPort?.inputStream
            outputStream = serialPort?.outputStream
        } catch (e: Exception) {
            Log.e(TAG, "init: ", e)
        }
    }


    var pause = false
    var isRunning = true

    @ExperimentalUnsignedTypes
    fun start(): Flow<UByteArray> {
        val inputStream = inputStream
            ?: throw RuntimeException("You should initialize the serial port before running")
        return flow {
            while (true) {
                if (!isRunning) break
                if (pause) continue
                try {
                    if (inputStream.available() > 0) {
//                        delay(200)
                        val byte = ByteArray(inputStream.available())
                        val len = inputStream.read(byte).coerceAtLeast(0)
                        emit(byte.sliceArray(0 until len))
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "while: ", e)
                }
            }
        }.map {
            it.toUByteArray()
        }.flowOn(Dispatchers.Default)
    }

    @ExperimentalUnsignedTypes
    suspend fun read(): UByteArray = withTimeout(100L) {
        val inputStream: InputStream = inputStream
            ?: throw RuntimeException("You should initialize the serial port before running")
        while (true) {
            if (inputStream.available() > 0) {
                val byte = ByteArray(1024)
                val len = inputStream.read(byte).coerceAtLeast(0)
                return@withTimeout byte.sliceArray(0 until len).toUByteArray()
            }
        }
        return@withTimeout ubyteArrayOf()
    }

    @ExperimentalUnsignedTypes
    suspend fun write(command: Command) = withContext(Dispatchers.IO) {
        val outputStream = outputStream
            ?: throw RuntimeException("You should initialize the serial port before running")
        try {
            outputStream.write(command.toUBytes().toByteArray())
            Log.d(TAG, "write: cmd = ${command.toUBytes().joinToHexString()}")
        } catch (e: Exception) {
            Log.e(TAG, "write: ", e)
        }
    }

    fun close() {
        isRunning = false
        serialPort?.inputStream?.close()
        serialPort?.outputStream?.close()
        inputStream = null
        outputStream = null
        serialPort?.close()
    }
}