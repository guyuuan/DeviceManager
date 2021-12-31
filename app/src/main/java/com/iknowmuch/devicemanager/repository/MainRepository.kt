package com.iknowmuch.devicemanager.repository

import com.iknowmuch.devicemanager.bean.CabinetDoor
import com.iknowmuch.devicemanager.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope

/**
 *@author: Chen
 *@createTime: 2021/10/12 14:26
 *@description:
 **/
class MainRepository @ExperimentalUnsignedTypes constructor(
    private val dataBaseRepository: DoorDataBaseRepository,
    private val serialPortDataRepository: SerialPortDataRepository,
    private val apiRepository: CabinetApiRepository
) {
    suspend fun insertCabinetDoor(data: List<CabinetDoor>) =
        dataBaseRepository.insertCabinetDoor(data)

    fun getCabinetDoorFlow() = dataBaseRepository.getCabinetDoorFlow()

    suspend fun updateCabinetDoorById(id: Int, modifier: (CabinetDoor) -> CabinetDoor) =
        dataBaseRepository.updateCabinetDoorById(id, modifier)

    @ExperimentalUnsignedTypes
    fun startDataAutoUpdate(
        preferenceManager: PreferenceManager,
        coroutineScope: CoroutineScope,
        totalChargingTime: Long
    ) =
        dataBaseRepository.startDataAutoUpdate(
            apiRepository,
            serialPortDataRepository,
            preferenceManager,
            coroutineScope,
            totalChargingTime
        )

    suspend fun updateLocaleData(
        deviceRepository: DeviceRepository
    ) = apiRepository.updateLocaleData(dataBaseRepository, deviceRepository)

    suspend fun heartBeat() = apiRepository.heartBeat(getAllDoorStatus())

    suspend fun returnProbe(probeCode: String) = apiRepository.returnProbe(probeCode)

    suspend fun updateRecordReport(state: Int, version: String, updateTime: String) =
        apiRepository.reportUpdateResult(state, version, updateTime)

    @ExperimentalUnsignedTypes
    fun getAllDoorStatus(): List<Map<String, Any>> {
        val list = mutableListOf<Map<String, Any>>()
        repeat(6){no->
            list+=mapOf(
                "doorNo" to no+1,
                "doorStatus" to serialPortDataRepository.checkDoorState(no+1),
                "probeStatus" to serialPortDataRepository.checkProbeState(no+1)
            )
        }
        return  list
    }
}