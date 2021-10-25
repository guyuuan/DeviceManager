package com.iknowmuch.devicemanager.repository

import com.iknowmuch.devicemanager.http.api.DoorApi
import com.iknowmuch.devicemanager.preference.PreferenceManager
import me.pqpo.librarylog4a.Log4a

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

    suspend fun reportDoorState(state: Int, deptId: String, doorNo: Int, probeState: Boolean) =
        try {
            doorApi.reportDoorState(
                state = state,
                deptId = deptId,
                deviceID = preferenceManager.deviceID,
                doorNo = doorNo,
                probeState = probeState
            )
        } catch (e: Exception) {
            Log4a.e(TAG, "closeDoor: ", e)
        }
}