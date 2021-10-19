package com.iknowmuch.devicemanager.repository

import com.iknowmuch.devicemanager.http.api.DoorApi
import com.iknowmuch.devicemanager.preference.PreferenceManager

/**
 *@author: Chen
 *@createTime: 2021/10/8 11:55
 *@description:
 **/
class DoorApiRepository(
    private val doorApi: DoorApi,
    private val preferenceManager: PreferenceManager
) {
    suspend fun openDoor(state: Int, deptId: String, doorNo: Int) = doorApi.openDoor(
        state = state,
        deptId = deptId,
        deviceID = preferenceManager.deviceID,
        doorNo = doorNo
    )

    suspend fun closeDoor(state: Int, deptId: String, doorNo: Int,probeState:Boolean) = doorApi.closeDoor(
        state = state,
        deptId = deptId,
        deviceID = preferenceManager.deviceID,
        doorNo = doorNo,
        probeState = probeState
    )
}