package com.iknowmuch.devicemanager.ui.scene.home

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iknowmuch.devicemanager.Config
import com.iknowmuch.devicemanager.bean.CabinetDoor
import com.iknowmuch.devicemanager.preference.PreferenceManager
import com.iknowmuch.devicemanager.repository.CabinetDoorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val repository: CabinetDoorRepository
) : ViewModel() {
    val deviceID = preferenceManager.deviceID
    val cabinetDoorList = repository.getCabinetDoorFlow().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), emptyList()
    )
    init {
/*        val cabinetDoorList = listOf(
            CabinetDoor(
                id = 1, status = CabinetDoor.Status.Empty,
                probeCode = null,
                availableTime = null,
                devicePower = 0
            ), CabinetDoor(
                id = 2, status = CabinetDoor.Status.Empty,
                probeCode = null,
                availableTime = null,
                devicePower = 0
            ), CabinetDoor(
                id = 3, status = CabinetDoor.Status.Empty,
                probeCode = null,
                availableTime = null,
                devicePower = 0
            ), CabinetDoor(
                id = 4, status = CabinetDoor.Status.Empty,
                probeCode = null,
                availableTime = null,
                devicePower = 0
            ), CabinetDoor(
                id = 5, status = CabinetDoor.Status.Empty,
                probeCode = null,
                availableTime = null,
                devicePower = 0
            ), CabinetDoor(
                id = 6, status = CabinetDoor.Status.Empty,
                probeCode = null,
                availableTime = null,
                devicePower = 0
            ))
        viewModelScope.launch {
            repository.insertCabinetDoor(cabinetDoorList)
        }*/
        repository.startDataAutoUpdate(
            viewModelScope,
            (preferenceManager.chargingTime * Config.Hour).roundToLong()
        )
//        viewModelScope.launch {
//            repository.updateCabinetDoor(1) {
//                it?.copy(
//                    devicePower = 0,
//                    status = CabinetDoor.Status.Charging,
//                    availableTime = 0f,
//                    remainingChargingTime = (preferenceManager.chargingTime * Config.Hour).roundToLong()
//                ) ?: throw RuntimeException("Couldn't get cabinet door by id = 1")
//            }
//        }
    }
}