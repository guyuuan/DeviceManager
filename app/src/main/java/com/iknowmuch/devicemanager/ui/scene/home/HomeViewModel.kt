package com.iknowmuch.devicemanager.ui.scene.home

import androidx.lifecycle.ViewModel
import com.iknowmuch.devicemanager.bean.CabinetDoor
import com.iknowmuch.devicemanager.preference.DeviceIDPreference
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 *@author: Chen
 *@createTime: 2021/9/15 10:47
 *@description:
 **/
@HiltViewModel
class HomeViewModel @Inject constructor(
    deviceIDPreference: DeviceIDPreference
) : ViewModel() {
    val deviceID by deviceIDPreference
    val cabinetDoorList = listOf(
        CabinetDoor(
            id = 1, status = CabinetDoor.Status.Idle,
            deviceCode = "181903FB",
            availableTime = 4f,
            devicePower = 100
        ),
        CabinetDoor(
            id = 2, status = CabinetDoor.Status.Charging,
            deviceCode = "141903FB",
            availableTime = 2f,
            devicePower = 50
        ),
        CabinetDoor(
            id = 3, status = CabinetDoor.Status.Fault,
            deviceCode = "851903FB",
            availableTime = 3f,
            devicePower = 80
        ),
        CabinetDoor(
            id = 4, status = CabinetDoor.Status.Error,
            deviceCode = "661903FB",
            availableTime = 3.5f,
            devicePower = 80
        ),
        CabinetDoor(
            id = 5, status = CabinetDoor.Status.Booked,
            deviceCode = "181903FB",
            availableTime = 4f,
            devicePower = 100
        ),
        CabinetDoor(
            id = 6, status = CabinetDoor.Status.Empty,
            deviceCode = null,
            availableTime = null,
            devicePower = 0
        ),
    )
}