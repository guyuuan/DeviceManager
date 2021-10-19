package com.iknowmuch.devicemanager.repository

import android.util.Log
import com.iknowmuch.devicemanager.http.api.DoorApi
import com.iknowmuch.devicemanager.preference.PreferenceManager

/**
 *@author: Chen
 *@createTime: 2021/10/8 11:55
 *@description:
 **/
private const val TAG = "DoorApiRepository"

class DoorApiRepository(
    private val doorApi: DoorApi,
    private val preferenceManager: PreferenceManager
) {
    suspend fun openDoor(state: Int, deptId: String, doorNo: Int) = try {
        doorApi.openDoor(
            state = state,
            deptId = deptId,
            deviceID = preferenceManager.deviceID,
            doorNo = doorNo
        )
    } catch (e: Exception) {
        Log.e(TAG, "openDoor: ", e)
    }

    suspend fun closeDoor(state: Int, deptId: String, doorNo: Int, probeState: Boolean) = try {
        doorApi.closeDoor(
            state = state,
            deptId = deptId,
            deviceID = preferenceManager.deviceID,
            doorNo = doorNo,
            probeState = probeState
        )
    } catch (e: Exception) {
        Log.e(TAG, "closeDoor: ", e)
    }
}