package com.iknowmuch.devicemanager.repository

import com.iknowmuch.devicemanager.bean.CabinetDoor
import com.iknowmuch.devicemanager.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope

/**
 *@author: Chen
 *@createTime: 2021/10/12 14:26
 *@description:
 **/
class MainRepository(
    private val dataBaseRepository: DoorDataBaseRepository,
    private val apiRepository: CabinetApiRepository
) {
    suspend fun insertCabinetDoor(data: List<CabinetDoor>) =
        dataBaseRepository.insertCabinetDoor(data)

    fun getCabinetDoorFlow() = dataBaseRepository.getCabinetDoorFlow()

    suspend fun updateCabinetDoorById(id: Int, modifier: (CabinetDoor) -> CabinetDoor) =
        dataBaseRepository.updateCabinetDoorById(id, modifier)

    fun startDataAutoUpdate(
        preferenceManager: PreferenceManager,
        coroutineScope: CoroutineScope,
        totalChargingTime: Long
    ) =
        dataBaseRepository.startDataAutoUpdate(
            apiRepository,
            preferenceManager,
            coroutineScope,
            totalChargingTime
        )

    suspend fun updateLocaleData(
        deviceRepository: DeviceRepository
    ) = apiRepository.updateLocaleData(dataBaseRepository, deviceRepository)

}