package com.iknowmuch.devicemanager.repository

import android.util.Log
import com.iknowmuch.devicemanager.bean.CabinetDoor
import com.iknowmuch.devicemanager.db.dao.CabinetDoorDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
        if (old!=null){
            Log.d(TAG, "updateCabinetDoorById: old $old")
            cabinetDoorDao.updateCabinetDoor(modifier(old))
        }else{
            Log.d(TAG, "updateCabinetDoorById: $id is null")
        }
    }

    suspend fun updateCabinetDoor(new: CabinetDoor) = cabinetDoorDao.updateCabinetDoor(new)

    fun startDataAutoUpdate(coroutineScope: CoroutineScope, totalChargingTime: Long) {
        coroutineScope.launch(Dispatchers.IO) {
            while (true) {
                cabinetDoorDao.getCabinetDoors().forEach {
                    when (it.status) {
                        CabinetDoor.Status.Booked,
                        CabinetDoor.Status.Empty,
                        CabinetDoor.Status.Error,
                        CabinetDoor.Status.Fault,
                        CabinetDoor.Status.Idle -> {
                        }
                        CabinetDoor.Status.Charging -> {
                            if (it.remainingChargingTime > 0) {
                                val time = (it.remainingChargingTime - DelayTime).coerceAtLeast(0)
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
                delay(DelayTime)
            }
        }
    }

}