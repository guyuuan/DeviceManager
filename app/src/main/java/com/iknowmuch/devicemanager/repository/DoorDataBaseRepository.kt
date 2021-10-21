package com.iknowmuch.devicemanager.repository

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.iknowmuch.devicemanager.bean.CabinetDataJson
import com.iknowmuch.devicemanager.bean.CabinetDoor
import com.iknowmuch.devicemanager.db.dao.CabinetDoorDao
import com.iknowmuch.devicemanager.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 *@author: Chen
 *@createTime: 2021/9/23 11:29
 *@description:
 **/
const val DelayTime = 1000L * 10
private const val TAG = "DoorDataBaseRepository"

class DoorDataBaseRepository(private val cabinetDoorDao: CabinetDoorDao) {
    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SS")

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what in 1..6) {
                msg.callback.run()
            }
        }
    }

    suspend fun insertCabinetDoor(data: List<CabinetDoor>) = cabinetDoorDao.insertCabinetDoors(data)

    fun getCabinetDoorFlow() = cabinetDoorDao.getCabinetDoorFlow()

//    private suspend fun updateCabinetDoorInfo(door: CabinetDoor) =
//        cabinetDoorDao.updateCabinetDoor(door)

    private suspend fun getCabinetDoorById(id: Int) = cabinetDoorDao.getCabinetDoorById(id)

//    suspend fun deleteCabinetDoor(door: CabinetDoor) = cabinetDoorDao.deleteCabinetDoor(door)

    suspend fun updateCabinetDoorById(id: Int, modifier: (CabinetDoor) -> CabinetDoor) {
        val old = getCabinetDoorById(id)
        if (old != null) {
            Log.d(TAG, "updateCabinetDoorById: old $old")
            cabinetDoorDao.updateCabinetDoor(modifier(old))
        } else {
            Log.d(TAG, "updateCabinetDoorById: $id is null")
        }
    }

    suspend fun updateCabinetDoor(new: CabinetDoor) = cabinetDoorDao.updateCabinetDoor(new)
    private var oldData: CabinetDataJson? = null

    //值为充电异常的设备id
    private val abnormalChargingCache = HashSet<String>()

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
                    for (it in cabinetDoorDao.getCabinetDoors()) {
                        if (it.status != CabinetDoor.Status.Empty && it.status != CabinetDoor.Status.Disabled) {
                            val probeState = serialPortDataRepository.checkProbeState(it.id)
                            val doorState = serialPortDataRepository.checkDoorState(it.id)
                            when {
                                doorState && probeState -> {
                                    it.probeCode?.let { code ->
                                        if (abnormalChargingCache.contains(code)) {
                                            apiRepository.reportAbnormalCharging(
                                                code, createTime = sdf.format(Date()), 3
                                            )?.status?.let { status ->
                                                if (status == 200) {
                                                    abnormalChargingCache.remove(code)
                                                    cabinetDoorDao.updateCabinetDoor(
                                                        it.copy(
                                                            status = CabinetDoor.Status.Charging
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                        if (doorOpenErrorCache.containsKey(it.id)) {
                                            apiRepository.clearDoorOpenAlarm(it.id)?.status?.let { status ->
                                                if (status == 200) {
                                                    doorOpenErrorCache.remove(it.id)
                                                    cabinetDoorDao.updateCabinetDoor(
                                                        it.copy(
                                                            status = CabinetDoor.Status.Charging
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                    }
                                    handler.removeMessages(it.id)
                                    doorOpenErrorCache.remove(it.id)
                                }
                                !probeState && doorState -> {
                                    //设备不在线,需要上报设备充电异常
                                    coroutineScope.launch(Dispatchers.IO) {
                                        it.probeCode?.let { code ->
                                            apiRepository.reportAbnormalCharging(
                                                code,
                                                createTime = sdf.format(System.currentTimeMillis()),
                                                4
                                            )?.status?.let {
                                                if (it == 200) {
                                                    abnormalChargingCache.add(code)
                                                }
                                            }
                                        }
                                    }

                                }
                                !doorState && probeState -> {
                                    val pair = doorOpenErrorCache[it.id]
                                    //如果为空,则没找到这个柜门的异常,是首次上报
                                    val callback = if (pair == null) {
                                        Runnable {
                                            coroutineScope.launch(Dispatchers.IO) {
                                                val time = System.currentTimeMillis()
                                                apiRepository.reportDoorOpenAlarm(
                                                    it.id,
                                                    createTime = sdf.format(time),
                                                    lastTime = sdf.format(time)
                                                )?.status?.let { status ->
                                                    if (status == 200) {
                                                        doorOpenErrorCache[it.id] = time to time
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Runnable {
                                            coroutineScope.launch(Dispatchers.IO) {
                                                val time = System.currentTimeMillis()
                                                val lastTime = pair.second
                                                if (lastTime == 0L) return@launch
                                                //距上次上报的时间大于十分钟后再上报
                                                if (time - lastTime > 10 * 60 * 1000L) {
                                                    apiRepository.reportDoorOpenAlarm(
                                                        it.id,
                                                        sdf.format(pair.first),
                                                        sdf.format(time)
                                                    )
                                                    doorOpenErrorCache[it.id] = pair.first to time
                                                }
                                            }
                                        }
                                    }
                                    handler.sendMessageDelayed(
                                        Message.obtain(handler, callback)
                                            .apply {
                                                doorOpenErrorCache[it.id] = 0L to 0L
                                                what = it.id
                                            }, 60 * 1000L
                                    )

                                }
                                else -> {
                                    //门没有关,设备也不在线
                                }
                            }
                            if (!doorState || !probeState) {
                                cabinetDoorDao.updateCabinetDoor(
                                    it.copy(
                                        status = CabinetDoor.Status.Error
                                    )
                                )
                                continue
                            }
                        }
//                        probes.add(
//                            CabinetDataJson.Probe(
//                                doorNO = it.id,
//                                probeCode = it.probeCode,
//                                availableTime = it.availableTime?.toInt(),
//                                power = it.devicePower
//                            )
//                        )
                        when (it.status) {
                            CabinetDoor.Status.Disabled,
                            CabinetDoor.Status.Booked,
                            CabinetDoor.Status.Empty,
                            CabinetDoor.Status.Error,
                            CabinetDoor.Status.Fault,
                            CabinetDoor.Status.Idle -> {
                            }
                            CabinetDoor.Status.Charging -> {

                                val remainingChargingTime = it.remainingChargingTime
                                    ?: (totalChargingTime * (1f - it.devicePower / 100f)).roundToLong()
                                if (remainingChargingTime > 0) {
                                    val time =
                                        (remainingChargingTime - DelayTime).coerceAtLeast(0)
                                    val percent = (1f - (time / totalChargingTime.toFloat()))
                                    val new = it.copy(
                                        remainingChargingTime = time,
                                        devicePower = (100 * percent).roundToInt(),
                                        status = if (time > 0) it.status else CabinetDoor.Status.Idle,
                                        availableTime = 4f * percent
                                    )
                                    cabinetDoorDao.updateCabinetDoor(new)
                                } else {
                                    cabinetDoorDao.updateCabinetDoor(it.copy(status = CabinetDoor.Status.Idle))
                                    serialPortDataRepository.stopCharging(it.id)
                                }
                            }
                        }
                    }
                    try {
                        val probes = cabinetDoorDao.getChargingCabinetDoors().map {
                            CabinetDataJson.Probe(
                                doorNO = it.id,
                                probeCode = it.probeCode,
                                availableTime = it.availableTime?.toInt(),
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
                                Log.e(TAG, "reportLocaleData: ", e)
                            } finally {
                                oldData = newData
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "loop: ", e)
                    }
                }
                delay(DelayTime)
            }
        }
    }

}