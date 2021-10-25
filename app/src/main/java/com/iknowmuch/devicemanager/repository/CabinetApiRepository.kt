package com.iknowmuch.devicemanager.repository

import android.annotation.SuppressLint
import com.blankj.utilcode.util.AppUtils
import com.iknowmuch.devicemanager.bean.CabinetDataJson
import com.iknowmuch.devicemanager.bean.CabinetDoor
import com.iknowmuch.devicemanager.bean.HomeDataJson
import com.iknowmuch.devicemanager.http.api.CabinetApi
import com.iknowmuch.devicemanager.preference.PreferenceManager
import me.pqpo.librarylog4a.Log4a
import java.text.SimpleDateFormat

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
    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    private suspend fun getHomeData(): HomeDataJson? {
        return try {
            if (preferenceManager.deptID.isNotEmpty()) {
                cabinetApi.getHomeData(preferenceManager.deviceID, preferenceManager.deptID)
            } else {
                null
            }
        } catch (e: Exception) {
            Log4a.e(TAG, "getHomeData: ", e)
            null
        }
    }

    //0可借用，1使用中，2被预定，3充电中，4有故障，5遗失，6异常，7归还异常
    suspend fun updateLocaleData(
        doorDataBaseRepository: DoorDataBaseRepository,
        deviceRepository: DeviceRepository
    ) {
        val homeData = getHomeData()
        if (homeData == null || homeData.data?.data.isNullOrEmpty()) {
            deviceRepository.updateDeviceInfo()
        }
        homeData?.data?.let {
            val list = it.data
            list.forEachIndexed { i, e ->
                if (i == 0) deviceRepository.updateDeviceInfo(
                    name = e.cabinetName,
                    location = e.cabinetAddress,
                    enabled = e.cabinetEnable == "0"
                )
                Log4a.d(TAG, "message: ${e.message}")
                e.message.let { data ->
                    if (data != null) {
                        doorDataBaseRepository.updateCabinetDoorById(data.cabinetDoorNo) { door ->
                            door.copy(
                                devicePower = try {
                                    (data.power ?: "0").toInt()
                                } catch (e: Exception) {
                                    Log4a.e(TAG, "updateLocaleData: ", e)
                                    0
                                },
                                availableTime = try {
                                    data.availableTime?.split("小时")?.firstOrNull()?.toFloat()
                                } catch (e: Exception) {
                                    Log4a.e(TAG, "updateLocaleData: ", e)
                                    null
                                } ?: 0f,
                                probeName = data.probeName,
                                probeCode = data.probeCode,
                                status = if (e.cabinetDoorState == 0) {
                                    if (e.closeState == 1) {
                                        CabinetDoor.Status.Error
                                    } else {
                                        when (data.probeState) {
                                            0 -> CabinetDoor.Status.Idle
                                            1 -> CabinetDoor.Status.Empty
                                            2 -> CabinetDoor.Status.Booked
                                            3 -> CabinetDoor.Status.Charging
                                            4 -> CabinetDoor.Status.Fault
                                            else -> CabinetDoor.Status.Error
                                        }
                                    }
                                } else CabinetDoor.Status.Disabled
                            )
                        }
                    } else {
                        doorDataBaseRepository.updateCabinetDoorById(e.cabinetDoorCode) { door ->
                            door.copy(
                                devicePower = 0,
                                availableTime = null,
                                remainingChargingTime = null,
                                probeCode = null,
                                probeName = null,
                                status = if (e.cabinetDoorState == 1) CabinetDoor.Status.Disabled else if (e.closeState == 1) CabinetDoor.Status.Error else CabinetDoor.Status.Empty
                            )
                        }
                    }
                }
            }
        }
    }

    suspend fun reportLocaleData(data: CabinetDataJson) = cabinetApi.reportCabinetData(data)

    suspend fun heartBeat() = try {
        cabinetApi.heartBeat(
            mapOf(
                "code" to preferenceManager.deviceID,
                "updateTime" to sdf.format(System.currentTimeMillis()),
                "deptId" to preferenceManager.deptID,
                "appVersion" to AppUtils.getAppVersionName()
            )
        )
    } catch (e: Exception) {
        Log4a.e(TAG, "heartBeat: ", e)
    }

    suspend fun reportAbnormalCharging(
        probeCode: String,
        createTime: String,
        lastTime: String,
    ) = reportAlarm(
        createTime = createTime,
        lastTime = lastTime,
        type = "0",
        state = "9",
        content = "设备$probeCode 充电异常",
        probeCode = probeCode
    )

    suspend fun reportDoorOpenAlarm(
        doorNo: Int, createTime: String, lastTime: String,
    ) = reportAlarm(
        createTime = createTime, lastTime = lastTime,
        type = "1",
        state = "0",
        content = "柜门$doorNo 未关闭", probeCode = "",
        cabinetDoorNo = doorNo.toString()
    )

    suspend fun clearDoorOpenAlarm(
        doorNo: Int
    ) = reportAlarm(
        createTime = "", lastTime = "",
        type = "1",
        state = "3",
        content = "柜门$doorNo 已关闭", probeCode = "",
        cabinetDoorNo = doorNo.toString()
    )

    private suspend fun reportAlarm(
        cabinetCode: String = preferenceManager.deviceID,
        deptId: String = preferenceManager.deptID,
        createTime: String,
        type: String,
        probeCode: String,
        state: String,
        content: String,
        lastTime: String,
        cabinetDoorNo: String = ""
    ) =
        try {
            cabinetApi.reportAlarm(
                mapOf(
                    "type" to type,
                    "cabinetCode" to cabinetCode,
                    "probeCode" to probeCode, "state" to state,
                    "content" to content,
                    "deptId" to deptId,
                    "createTime" to createTime,
                    "lastTime" to lastTime,
                    "cabinetDoorNo" to cabinetDoorNo
                )
            )
        } catch (e: Exception) {
            Log4a.e(TAG, "reportAlarm: ", e)
            null
        }

    suspend fun returnProbe(probeCode: String) = cabinetApi.returnProbe(
        probeCode = probeCode,
        cabinetCode = preferenceManager.deviceID,
        deptId = preferenceManager.deptID
    )

    suspend fun reportUpdateResult(state: Int, version: String, updateTime: String) =
        cabinetApi.reportUpdateResult(
            mapOf(
                "upGradeStatus" to state.toString(),
                "appVersion" to version,
                "upGradeTime" to updateTime,
                "code" to preferenceManager.deviceID,
                "deptId" to preferenceManager.deptID
            )
        )
}