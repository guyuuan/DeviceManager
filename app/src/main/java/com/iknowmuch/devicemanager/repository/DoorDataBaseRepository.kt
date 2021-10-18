package com.iknowmuch.devicemanager.repository

import android.util.Log
import com.iknowmuch.devicemanager.bean.CabinetDataJson
import com.iknowmuch.devicemanager.bean.CabinetDoor
import com.iknowmuch.devicemanager.db.dao.CabinetDoorDao
import com.iknowmuch.devicemanager.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    fun startDataAutoUpdate(
        apiRepository: CabinetApiRepository,
        preferenceManager: PreferenceManager,
        coroutineScope: CoroutineScope,
        totalChargingTime: Long
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            delay(DelayTime)
            while (true) {
                if (preferenceManager.deptID.isNotEmpty()) {
                    val probes = mutableListOf<CabinetDataJson.Probe>()
                    cabinetDoorDao.getCabinetDoors().forEach {
                        probes.add(
                            CabinetDataJson.Probe(
                                doorNO = it.id,
                                probeCode = it.probeCode,
                                availableTime = it.availableTime?.toInt(),
                                power = it.devicePower
                            )
                        )
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
                                    val time = (remainingChargingTime - DelayTime).coerceAtLeast(0)
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
                                }
                            }
                        }
                    }
                    try {
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