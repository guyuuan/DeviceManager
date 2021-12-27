package com.iknowmuch.devicemanager.repository

import com.iknowmuch.devicemanager.bean.Device
import com.iknowmuch.devicemanager.db.dao.DeviceDao
import com.iknowmuch.devicemanager.preference.PreferenceManager

/**
 *@author: Chen
 *@createTime: 2021/10/11 9:31
 *@description:
 **/
class DeviceRepository(
    private val deviceDao: DeviceDao,
    private val preferenceManager: PreferenceManager
) {

    fun getDeviceInfo() = deviceDao.getDeviceInfo()

    suspend fun getCurrentDeviceInfo() = deviceDao.getCurrentDeviceInfo()
    suspend fun insertDeviceInfo() {
        deviceDao.insertDeviceInfo(Device(primaryKey = 1))
    }

    suspend fun updateDeviceInfo(
        name: String = "智能柜",
        enabled: Boolean = false,
        location: String = "未知"
    ) =
        deviceDao.updateDeviceInfo(
            Device(
                primaryKey = 1,
                name = name,
                id = preferenceManager.deviceID,
                location = location,enabled = enabled
            )
        )
}