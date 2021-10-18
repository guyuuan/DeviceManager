package com.iknowmuch.devicemanager.repository

import android.util.Log
import com.iknowmuch.devicemanager.bean.CabinetDataJson
import com.iknowmuch.devicemanager.bean.CabinetDoor
import com.iknowmuch.devicemanager.bean.HomeDataJson
import com.iknowmuch.devicemanager.http.api.CabinetApi
import com.iknowmuch.devicemanager.preference.PreferenceManager

/**
 *@author: Chen
 *@createTime: 2021/10/9 17:08
 *@description:
 **/
private const val TAG = "CabinetApiRepository"

class CabinetApiRepository(
    private val cabinetApi: CabinetApi,
    private val preferenceManager: PreferenceManager
) {
    private suspend fun getHomeData(): HomeDataJson? {
        return if (preferenceManager.deptID.isNotEmpty()) {
            cabinetApi.getHomeData(preferenceManager.deviceID, preferenceManager.deptID)
        } else {
            null
        }
    }

    //0可借用，1使用中，2被预定，3充电中，4有故障，5遗失，6异常，7归还异常
    suspend fun updateLocaleData(
        doorDataBaseRepository: DoorDataBaseRepository,
        deviceRepository: DeviceRepository
    ) {
        getHomeData()?.data?.let {
            val list = it.data
            list.forEachIndexed { i, e ->
                if (i == 0) deviceRepository.updateDeviceInfo(
                    name = e.cabinetName,
                    location = e.cabinetAddress
                )
                Log.d(TAG, "message: ${e.message}")
                e.message.let { data ->
                    if (data != null) {
                        doorDataBaseRepository.updateCabinetDoorById(data.cabinetDoorNo) { door ->
                            door.copy(
                                devicePower = data.power ?: 0,
                                availableTime = data.availableTime?.toFloat() ?: 0f,
                                probeCode = data.probeCode,
                                status = if (e.cabinetDoorState == 0) when (data.probeState) {
                                    0 -> CabinetDoor.Status.Idle
                                    1 -> CabinetDoor.Status.Empty
                                    2 -> CabinetDoor.Status.Booked
                                    3 -> CabinetDoor.Status.Charging
                                    4 -> CabinetDoor.Status.Fault
                                    else -> CabinetDoor.Status.Error
                                } else CabinetDoor.Status.Disabled
                            )
                        }
                    } else {
                        doorDataBaseRepository.updateCabinetDoorById(e.cabinetDoorCode) { door ->
                            door.copy(
                                devicePower = 0,
                                availableTime = null,
                                probeCode = null,
                                status = if (e.cabinetDoorState == 1) CabinetDoor.Status.Disabled else CabinetDoor.Status.Empty
                            )
                        }
                    }
                }
            }
        }
    }

    suspend fun reportLocaleData(data: CabinetDataJson) = cabinetApi.reportCabinetData(data)
}