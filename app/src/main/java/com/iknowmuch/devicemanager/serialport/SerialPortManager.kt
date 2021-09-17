package com.iknowmuch.devicemanager.serialport

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 *@author: Chen
 *@createTime: 2021/9/16 13:25
 *@description:
 **/
private const val TAG = "SerialPortManager"

class SerialPortManager {
    //    private val serialPort = SerialPort(File("/dev/usb_accessory"), 115200, 0)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun run() {
        coroutineScope.launch(Dispatchers.IO) {
//            val input = (serialPort.inputStream as FileInputStream)
            while (true) {
                Log.d(
                    TAG, "run: $"
                )
                delay(1000L)
            }
        }
    }
}