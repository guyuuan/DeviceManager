package com.iknowmuch.devicemanager.repository

import android.util.Log
import com.iknowmuch.devicemanager.bean.ControllerResult
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.shareIn
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

    private val _controlDoorResult = MutableStateFlow(ControllerResult())
    val controlResult: StateFlow<ControllerResult> get() = _controlDoorResult
    private val stateMap = mapOf(
        Command.CMD.DoorState to Array(6) { ubyteArrayOf() },
        Command.CMD.ProbeState to Array(6) { ubyteArrayOf() },
        Command.CMD.Open to Array(1) { ubyteArrayOf() }
    )

    @ExperimentalUnsignedTypes
    private val serialPortData by lazy {
        //使用ShardFlow保存数据,并设置最多可缓存100条指令
        serialPortManager.start().shareIn(coroutineScope, SharingStarted.Eagerly, 100)
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
                                stateMap[Command.CMD.DoorState]?.get(doorId)
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
                                val doorId = cmd.data[0].toInt() - 1
                                stateMap[Command.CMD.ProbeState]?.set(
                                    doorId,
                                    cmd.data.toUByteArray()
                                )
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
                fetchDoorState()
                fetchProbeStatus()
            }
        }
    }

    //返回true代表状态正常,不需要警报
    //data第一位为获取柜门状态成功(1)或失败(0),第二位是柜门号,最后一位为0锁打开,1锁关闭
    fun checkDoorState(id: Int): Boolean {
        val data = stateMap[Command.CMD.DoorState]?.get(id - 1) ?: ubyteArrayOf()
        if (data.isEmpty()) return true
        return 1.toUByte() == data.firstOrNull() && 1.toUByte() == data.lastOrNull()
    }

    //data最后一位 为1代表设备在线,0代表设备离线
    fun checkProbeState(id: Int): Boolean {
        val data = stateMap[Command.CMD.ProbeState]?.get(id - 1) ?: ubyteArrayOf()
        if (data.isEmpty()) return true
        return 1.toUByte() == data.lastOrNull()
    }

    private suspend fun fetchDoorState() {
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
        val result = withTimeoutOrNull(2000L) {
            while (true) {
                delay(200L)
                val arr = stateMap[Command.CMD.Open]?.first()
                if (!arr.isNullOrEmpty()) {
                    return@withTimeoutOrNull (arr.first() == 1.toUByte()).also {
                        stateMap[Command.CMD.Open]?.set(0, ubyteArrayOf())
                    }
                }
            }
            false
        } ?: false

        return result
    }

    @ExperimentalCoroutinesApi
    suspend fun controlDoor(
        id: Int, state: Int,
        onOpen: suspend (Boolean) -> Unit,
        //关门是否成功,探头设备是否在线
        onClose: suspend (Boolean, Boolean) -> Unit
    ) {
        //开门是否成功回调
        val open = openDoor(id)
        _controlDoorResult.emit(
            ControllerResult(
                id,
                status = state,
                openState = open,
                closeState = false
            )
        )
        onOpen(open)
        Log.d(TAG, "lendDevice open: $open")
        if (open) {
            val close = withTimeoutOrNull(70 * 1000L) {
                //等待30s后再去判断柜门是否关上
                repeat(15) {
                    delay(4 * 1000L)
                    val arr = stateMap[Command.CMD.DoorState]?.get(id - 1)
                    if (!arr.isNullOrEmpty()) {
                        val checkResult =
                            1.toUByte() == arr.firstOrNull() && 1.toUByte() == arr.lastOrNull()
                        if (checkResult) {
                            return@withTimeoutOrNull (checkResult).also {
                                stateMap[Command.CMD.DoorState]?.set(id - 1, ubyteArrayOf())
                            }
                        } else if (it == 14 && !checkResult) {
                            return@withTimeoutOrNull (checkResult).also {
                                stateMap[Command.CMD.DoorState]?.set(id - 1, ubyteArrayOf())
                            }
                        }
                    }
                }
                return@withTimeoutOrNull false
            } ?: false
            onClose(close, checkProbeState(id))
            delay(10*1000L)
            _controlDoorResult.emit(_controlDoorResult.value.copy(doorNo = 0, closeState = close))
            Log.d(TAG, "lendDevice close: $close")
        }
    }

    suspend fun stopCharging(id: Int) = write(
        cmd = Command(
            cmd = Command.CMD.StopCharging.ubyte,
            data = arrayOf(id.toUByte())
        )
    )

    @ExperimentalUnsignedTypes
    suspend fun write(cmd: Command) = serialPortManager.write(cmd)

    @ExperimentalUnsignedTypes
    suspend fun fetchProbeStatus() {
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