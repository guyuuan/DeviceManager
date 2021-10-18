package com.iknowmuch.devicemanager.repository

import android.util.Log
import com.iknowmuch.devicemanager.preference.PreferenceManager
import com.iknowmuch.devicemanager.serialport.Command
import com.iknowmuch.devicemanager.serialport.SerialPortManager
import com.iknowmuch.devicemanager.serialport.joinToHexString
import com.iknowmuch.devicemanager.serialport.toCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 *@author: Chen
 *@createTime: 2021/10/18 9:18
 *@description:
 **/
private const val TAG = "SerialPortRepository"

@ExperimentalUnsignedTypes
class SerialPortDataRepository(
    private val serialPortManager: SerialPortManager,
    private val preferenceManager: PreferenceManager
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val stateMap = mapOf(
        Command.CMD.DoorState to Array(6) { ubyteArrayOf() },
        Command.CMD.ProbeState to Array(6) { ubyteArrayOf() },
        Command.CMD.Open to Array(1) { ubyteArrayOf() }
    )

    @ExperimentalUnsignedTypes
    private val serialPortData by lazy {
        serialPortManager.start().stateIn(coroutineScope, SharingStarted.Eagerly, ubyteArrayOf())
    }


    @ExperimentalUnsignedTypes
    fun init() {
        serialPortManager.init(preferenceManager.serialPortPath)
        coroutineScope.launch(Dispatchers.IO) {
            serialPortData.collect {
                Log.d(TAG, "get serial port message: ${it.joinToHexString()}")
                if (it.size > 7) {
                    try {
                        val cmd = it.toCommand()
                        when (cmd._cmd) {
                            Command.CMD.DoorState -> {
                                val doorId = cmd.data[1].toInt() - 1
                                Log.d("Door", "door state : id =$doorId, state = ${cmd.data.lastOrNull()?.toInt()} ")
                                stateMap[Command.CMD.DoorState]?.set(
                                    doorId,
                                    cmd.data.toUByteArray()
                                )
                            }
                            Command.CMD.Open -> {
                                Log.d(TAG, "collect open data: ")
                                stateMap[Command.CMD.Open]?.set(0, cmd.data.toUByteArray())
                            }
                            Command.CMD.OpenAll -> {
                            }
                            Command.CMD.ProbeState -> {
                            }
                            Command.CMD.StopCharging -> {
                            }
                            Command.CMD.Version -> {
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "collect message: ", e)
                    }
                }
            }
        }
        coroutineScope.launch(Dispatchers.IO) {
            while (true) {
                delay(5 * 1000L)
                checkDoorState()
                checkProbeStatus()
            }
        }
    }

    private suspend fun checkDoorState() {
        repeat(6) {
            delay(500)
            write(
                Command(
                    cmd = Command.CMD.DoorState.ubyte,
                    data = arrayOf(1.toUByte(), (it + 1).toUByte())
                )
            )
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun openDoor(id: Int): Boolean {
        write(
            Command(
                cmd = Command.CMD.Open.ubyte,
                data = arrayOf(0x01.toUByte(), 0x01.toUByte(), id.toUByte())
            )
        )
        val result = withTimeoutOrNull(2000) {
            while (true) {
                val arr = stateMap[Command.CMD.Open]?.first()
                if (!arr.isNullOrEmpty()) {
                    return@withTimeoutOrNull (arr.first() == 1.toUByte()).also {
                        stateMap[Command.CMD.Open]?.set(0, ubyteArrayOf())
                    }
                }
                delay(100L)
            }
            return@withTimeoutOrNull false
        } ?: false

        return result
    }

    @ExperimentalCoroutinesApi
    suspend fun lendDevice(
        id: Int,
        onOpen: suspend (Boolean) -> Unit,
        onClose: suspend (Boolean) -> Unit
    ) {
        //开门是否成功回调
        val open = openDoor(id)
        onOpen(open)
        Log.d(TAG, "lendDevice open: $open")
        if (open) {
            val close = withTimeoutOrNull(70 * 1000L) {
                //等待30s后再去判断柜门是否关上
                repeat(6) {
                    delay(10 * 1000L)
                    val arr = stateMap[Command.CMD.DoorState]?.get(id - 1)
                    if (!arr.isNullOrEmpty()) {
                        val checkResult =
                            1.toUByte() == arr.firstOrNull() && 1.toUByte() == arr.lastOrNull()
                        if (checkResult) {
                            return@withTimeoutOrNull (checkResult).also {
                                stateMap[Command.CMD.DoorState]?.set(id - 1, ubyteArrayOf())
                            }
                        } else if (it == 5 && !checkResult) {
                            return@withTimeoutOrNull (checkResult).also {
                                stateMap[Command.CMD.DoorState]?.set(id - 1, ubyteArrayOf())
                            }
                        }
                    }
                }
                return@withTimeoutOrNull false
            } ?: false
            onClose(close)
            Log.d(TAG, "lendDevice close: $close")
        }
    }

    @ExperimentalUnsignedTypes
    suspend fun write(com: Command) = serialPortManager.write(com)

    @ExperimentalUnsignedTypes
    suspend fun checkProbeStatus() {
        repeat(6) {
            delay(100)
            write(
                Command(
                    cmd = Command.CMD.ProbeState.ubyte,
                    data = arrayOf((it + 1).toUByte())
                )
            )
        }
    }

    fun close() {
        coroutineScope.cancel()
        serialPortManager.close()
    }
}