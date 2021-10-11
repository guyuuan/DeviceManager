package com.iknowmuch.devicemanager.ui.scene.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iknowmuch.devicemanager.Config
import com.iknowmuch.devicemanager.bean.Device
import com.iknowmuch.devicemanager.preference.PreferenceManager
import com.iknowmuch.devicemanager.repository.CabinetApiRepository
import com.iknowmuch.devicemanager.repository.DeviceRepository
import com.iknowmuch.devicemanager.repository.DoorDataBaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToLong

/**
 *@author: Chen
 *@createTime: 2021/9/15 10:47
 *@description:
 **/
private const val TAG = "HomeViewModel"

@HiltViewModel
class HomeViewModel @Inject constructor(
    preferenceManager: PreferenceManager,
    private val cabinetApiRepository: CabinetApiRepository,
    private val deviceRepository: DeviceRepository,
    private val repository: DoorDataBaseRepository
) : ViewModel() {
    val deviceID = preferenceManager.deviceID
    val cabinetDoorList = repository.getCabinetDoorFlow().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), emptyList()
    )

    val device = deviceRepository.getDeviceInfo().stateIn(
        viewModelScope, started = SharingStarted.Lazily,
        Device()
    )

    init {

        //0可借用，1使用中，2被预定，3充电中，4有故障，5遗失，6异常，7归还异常
        viewModelScope.launch(Dispatchers.IO) {
            try {
                cabinetApiRepository.updateData(repository, deviceRepository)
            } catch (e: Exception) {
                Log.e(TAG, "updateHomeData: ", e)
            }
        }
        repository.startDataAutoUpdate(
            viewModelScope,
            (preferenceManager.chargingTime * Config.Hour).roundToLong()
        )
        /*        val cabinetDoorList = listOf(
            CabinetDoor(
                id = 1, status = CabinetDoor.Status.Empty,
                probeCode = null,
                availableTime = null,
                devicePower = 0
            ),
            CabinetDoor(
                id = 2, status = CabinetDoor.Status.Empty,
                probeCode = null,
                availableTime = null,
                devicePower = 0
            ),
            CabinetDoor(
                id = 3, status = CabinetDoor.Status.Empty,
                probeCode = null,
                availableTime = null,
                devicePower = 0
            ),
            CabinetDoor(
                id = 4, status = CabinetDoor.Status.Empty,
                probeCode = null,
                availableTime = null,
                devicePower = 0
            ),
            CabinetDoor(
                id = 5, status = CabinetDoor.Status.Empty,
                probeCode = null,
                availableTime = null,
                devicePower = 0
            ),
            CabinetDoor(
                id = 6, status = CabinetDoor.Status.Empty,
                probeCode = null,
                availableTime = null,
                devicePower = 0
            ),
        )
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertCabinetDoor(cabinetDoorList)
            deviceRepository.insertDeviceInfo()
        }*/
    }
}