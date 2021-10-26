package com.iknowmuch.devicemanager.repository

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.iknowmuch.devicemanager.Config
import com.iknowmuch.devicemanager.bean.CabinetDataJson
import com.iknowmuch.devicemanager.bean.CabinetDoor
import com.iknowmuch.devicemanager.db.dao.CabinetDoorDao
import com.iknowmuch.devicemanager.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.pqpo.librarylog4a.Log4a
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 *@author: Chen
 *@createTime: 2021/9/23 11:29
 *@description:
 **/
const val DelayTime = 1000L * 10
private const val TAG = "DoorDataBaseRepository"

class DoorDataBaseRepository @ExperimentalUnsignedTypes constructor(
    private val cabinetDoorDao: CabinetDoorDao,
    serialPortDataRepository: SerialPortDataRepository
) {
    @ExperimentalUnsignedTypes
    private val controlResult = serialPortDataRepository.controlResult

    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (msg.what in 1..6) {
                super.handleMessage(msg)
            }
        }
    }

    suspend fun insertCabinetDoor(data: List<CabinetDoor>) = cabinetDoorDao.insertCabinetDoors(data)

    fun getCabinetDoorFlow() = cabinetDoorDao.getCabinetDoorFlow()

    private suspend fun getCabinetDoorById(id: Int) = cabinetDoorDao.getCabinetDoorById(id)

    suspend fun updateCabinetDoorById(id: Int, modifier: (CabinetDoor) -> CabinetDoor) {
        val old = getCabinetDoorById(id)
        if (old != null) {
            Log4a.d(TAG, "updateCabinetDoorById: old $old")
            cabinetDoorDao.updateCabinetDoor(modifier(old))
        } else {
            Log4a.d(TAG, "updateCabinetDoorById: $id is null")
        }
    }

//    suspend fun updateCabinetDoor(new: CabinetDoor) = cabinetDoorDao.updateCabinetDoor(new)

    private var oldData: CabinetDataJson? = null

    //值为充电异常的设备id
    private val abnormalChargingCache = HashMap<String, Pair<Long, Long>>()

    //柜门未关闭异常缓存,key为柜门id,value为首次发现异常时间和之后再次上报的时间
    private val doorOpenErrorCache = HashMap<Int, Pair<Long, Long>>()

    @ExperimentalUnsignedTypes
    fun startDataAutoUpdate(
        apiRepository: CabinetApiRepository,
        serialPortDataRepository: SerialPortDataRepository,
        preferenceManager: PreferenceManager,
        coroutineScope: CoroutineScope,
        totalChargingTime: Long
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            delay(DelayTime)
            while (true) {
                if (preferenceManager.deptID.isNotEmpty()) {
                    for (cabinetDoor in cabinetDoorDao.getCabinetDoors()) {
                        if (cabinetDoor.status != CabinetDoor.Status.Empty && cabinetDoor.status != CabinetDoor.Status.Disabled) {
                            val result = controlResult.value
                            val probeState =
                                serialPortDataRepository.checkProbeState(cabinetDoor.id)
                            val doorState = serialPortDataRepository.checkDoorState(cabinetDoor.id)
                            when {
                                doorState && probeState -> {
                                    //清除充电异常警报
                                    if (cabinetDoor.probeCode != null) {
                                        if (abnormalChargingCache.contains(cabinetDoor.probeCode) || cabinetDoor.status == CabinetDoor.Status.Error) {
                                            abnormalChargingCache.remove(cabinetDoor.probeCode)
                                            clearDoorOpenAlarm(
                                                doorState, probeState,
                                                apiRepository,
                                                cabinetDoor,
                                                totalChargingTime
                                            )
                                        }
                                    } else {
                                        //清除门未关异常
                                        if (doorOpenErrorCache.containsKey(cabinetDoor.id) || cabinetDoorDao.getCabinetDoorById(
                                                cabinetDoor.id
                                            )?.status == CabinetDoor.Status.Error
                                        ) {
                                            doorOpenErrorCache.remove(cabinetDoor.id)
                                            Log4a.d("TAG", "清除门未关异常: ")
                                            clearDoorOpenAlarm(
                                                doorState, probeState,
                                                apiRepository,
                                                cabinetDoor,
                                                totalChargingTime
                                            )
                                        }
                                    }
                                    handler.removeMessages(cabinetDoor.id)
                                }
                                !probeState && doorState && result.doorNo != cabinetDoor.id -> {
                                    //设备不在线,需要上报设备充电异常
                                    val code = cabinetDoor.probeCode
                                    if (code != null) {
                                        val pair = abnormalChargingCache[code]
                                        val callback = if (pair == null) {
                                            Runnable {
                                                handler.removeMessages(cabinetDoor.id)
                                                coroutineScope.launch(Dispatchers.IO) {
                                                    val time = System.currentTimeMillis()
                                                    apiRepository.reportAbnormalCharging(
                                                        code,
                                                        createTime = sdf.format(time),
                                                        lastTime = sdf.format(time)
                                                    )?.status?.let { status ->
                                                        if (status == 200) {
                                                            abnormalChargingCache[code] =
                                                                time to time
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            Runnable {
                                                if (abnormalChargingCache[code] == null) return@Runnable
                                                coroutineScope.launch(Dispatchers.IO) {
                                                    val currentTime = System.currentTimeMillis()
                                                    val lastTime = pair.second
                                                    if (lastTime == 0L) return@launch
                                                    if (handler.hasMessages(cabinetDoor.id)) {
                                                        handler.removeMessages(cabinetDoor.id)
                                                    }
                                                    if (currentTime - lastTime > Config.ErrorReportDelay) {
                                                        apiRepository.reportAbnormalCharging(
                                                            code,
                                                            createTime = sdf.format(pair.first),
                                                            lastTime = sdf.format(currentTime)
                                                        )?.status?.let { status ->
                                                            handler.removeMessages(cabinetDoor.id)
                                                            if (status == 200) {
                                                                abnormalChargingCache[code] =
                                                                    pair.first to currentTime
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        if (!handler.hasMessages(cabinetDoor.id)) {
                                            handler.sendMessageDelayed(
                                                Message.obtain(handler, callback)
                                                    .apply {
                                                        what = cabinetDoor.id
                                                    }, 1000L
                                            )
                                        }
                                    } else {
                                        clearDoorOpenAlarm(
                                            doorState, probeState,
                                            apiRepository,
                                            cabinetDoor,
                                            totalChargingTime
                                        )
                                    }
                                }
                                !doorState && result.doorNo != cabinetDoor.id -> {
                                    reportDoorError(
                                        cabinetDoor = cabinetDoor,
                                        coroutineScope = coroutineScope,
                                        apiRepository = apiRepository
                                    )
                                }
                                !doorState && !probeState && result.doorNo != cabinetDoor.id -> {
                                    //门没有关,设备也不在线
                                    reportDoorError(
                                        cabinetDoor = cabinetDoor,
                                        coroutineScope = coroutineScope,
                                        apiRepository = apiRepository
                                    )
                                }
                            }
                            if (!doorState || !probeState) {
                                continue
                            }
                        }
                        when (cabinetDoor.status) {
                            CabinetDoor.Status.Disabled,
                            CabinetDoor.Status.Booked,
                            CabinetDoor.Status.Empty,
                            CabinetDoor.Status.Error,
                            CabinetDoor.Status.Fault,
                            CabinetDoor.Status.Idle -> {
                            }
                            CabinetDoor.Status.Charging -> {
                                val remainingChargingTime =
                                    cabinetDoor.remainingChargingTime
                                        ?: (totalChargingTime * (1f - cabinetDoor.devicePower / 100f)).roundToLong()
                                if (remainingChargingTime > 0) {
                                    val time =
                                        (remainingChargingTime - DelayTime).coerceAtLeast(0)
                                    val percent =
                                        (1f - (time / totalChargingTime.toFloat())).coerceIn(0f..1f)
                                    val new = cabinetDoor.copy(
                                        remainingChargingTime = time,
                                        devicePower = (100 * percent).roundToInt(),
                                        availableTime = (totalChargingTime / Config.Minute.toFloat()) * (1f - percent)
                                    )
                                    cabinetDoorDao.updateCabinetDoor(new)
                                } else {
//                                    cabinetDoorDao.updateCabinetDoor(cabinetDoor.copy(status = CabinetDoor.Status.Idle))
                                    serialPortDataRepository.stopCharging(cabinetDoor.id)
                                }
                            }
                        }
                    }
                    try {
                        val probes = cabinetDoorDao.getChargingCabinetDoors().map {
                            CabinetDataJson.Probe(
                                doorNO = it.id,
                                probeCode = it.probeCode,
                                availableTime = it.availableTime?.toInt()?.toString() ?: "0",
                                power = it.devicePower
                            )
                        }
                        if (probes.isEmpty()) continue
                        val newData = CabinetDataJson(
                            cabinetCode = preferenceManager.deviceID,
                            deptId = preferenceManager.deptID.toInt(),
                            probes = probes
                        )
                        if (newData != oldData) {
                            try {
                                apiRepository.reportLocaleData(newData)
                            } catch (e: Exception) {
                                Log4a.e(TAG, "reportLocaleData: ", e)
                            } finally {
                                oldData = newData
                            }
                        }
                    } catch (e: Exception) {
                        Log4a.e(TAG, "loop: ", e)
                    }
                }
                delay(DelayTime)
            }
        }
    }

    private fun reportDoorError(
        coroutineScope: CoroutineScope,
        cabinetDoor: CabinetDoor,
        apiRepository: CabinetApiRepository
    ) {
        val pair = doorOpenErrorCache[cabinetDoor.id]

        //如果为空,则没找到这个柜门的异常,是首次上报
        val callback =
            if (pair == null && !handler.hasMessages(cabinetDoor.id)) {
                Runnable {
                    if (doorOpenErrorCache[cabinetDoor.id] != null) return@Runnable
                    handler.removeMessages(cabinetDoor.id)
                    coroutineScope.launch(Dispatchers.IO) {
                        val time = System.currentTimeMillis()
                        apiRepository.reportDoorOpenAlarm(
                            cabinetDoor.id,
                            createTime = sdf.format(time),
                            lastTime = sdf.format(time)
                        )?.status?.let { status ->
                            Log.d(
                                "TAG",
                                "startDataAutoUpdate: ${cabinetDoor.id} 未关门, 缓存数据 = ${doorOpenErrorCache[cabinetDoor.id]}, 上报结果 = $status"
                            )
                            if (status == 200) {
                                cabinetDoorDao.updateCabinetDoor(
                                    cabinetDoor.copy(
                                        status = CabinetDoor.Status.Error,
                                    )
                                )
                                doorOpenErrorCache[cabinetDoor.id] =
                                    time to time
                            }
                        }
                    }
                }
            } else {
                Runnable {
                    if (doorOpenErrorCache[cabinetDoor.id] == null) return@Runnable
                    coroutineScope.launch(Dispatchers.IO) {
                        val p = doorOpenErrorCache[cabinetDoor.id]
                            ?: return@launch
                        val currentTime = System.currentTimeMillis()
                        val lastTime = p.second
                        if (lastTime == 0L) return@launch
                        if (handler.hasMessages(cabinetDoor.id)) {
                            handler.removeMessages(cabinetDoor.id)
                        }
                        //距上次上报的时间大于十分钟后再上报
                        if (currentTime - lastTime > Config.ErrorReportDelay) {
                            apiRepository.reportDoorOpenAlarm(
                                cabinetDoor.id,
                                sdf.format(p.first),
                                sdf.format(currentTime)
                            )?.status?.let { status ->
                                handler.removeMessages(cabinetDoor.id)
                                Log.d(
                                    "TAG",
                                    "startDataAutoUpdate: ${cabinetDoor.id} 未关门, 缓存数据 = ${doorOpenErrorCache[cabinetDoor.id]}, 上报结果 = $status"
                                )
                                if (status == 200) {
                                    cabinetDoorDao.updateCabinetDoor(
                                        cabinetDoor.copy(
                                            status = CabinetDoor.Status.Error,
                                        )
                                    )
                                }
                            }
                            doorOpenErrorCache[cabinetDoor.id] =
                                p.first to currentTime
                        }
                    }
                }
            }
        if (!handler.hasMessages(cabinetDoor.id)) {
            handler.sendMessageDelayed(
                Message.obtain(handler, callback)
                    .apply {
                        what = cabinetDoor.id
                    }, 1000L
            )
        }
    }

    private suspend fun clearDoorOpenAlarm(
        doorState: Boolean, probeState: Boolean,
        apiRepository: CabinetApiRepository,
        cabinetDoor: CabinetDoor,
        totalChargingTime: Long
    ) {
        handler.removeMessages(cabinetDoor.id)
        apiRepository.clearDoorOpenAlarm(cabinetDoor.id)?.status?.let { status ->
            if (status == 200) {
                cabinetDoorDao.updateCabinetDoor(
                    cabinetDoor.copy(
                        status = if (cabinetDoor.probeCode != null) CabinetDoor.Status.Charging else CabinetDoor.Status.Empty,
                        devicePower = 0,
                        remainingChargingTime = null,
                        availableTime = totalChargingTime / Config.Minute.toFloat()
                    )
                )
                if (doorState) {
                    doorOpenErrorCache.remove(cabinetDoor.id)
                }
                if (probeState) {
                    abnormalChargingCache.remove(cabinetDoor.probeCode ?: return)
                }
            }
        }
    }

}