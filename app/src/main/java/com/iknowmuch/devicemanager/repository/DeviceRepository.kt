package com.iknowmuch.devicemanager.repository

import com.iknowmuch.devicemanager.bean.Device
import com.iknowmuch.devicemanager.db.dao.DeviceDao

/**
 *@author: Chen
 *@createTime: 2021/10/11 9:31
 *@description:
 **/
class DeviceRepository(private val deviceDao: DeviceDao) {

    fun getDeviceInfo() = deviceDao.getDeviceInfo()

    suspend fun insertDeviceInfo() {
        deviceDao.insertDeviceInfo(Device(primaryKey = 1))
    }

    suspend fun updateDeviceInfo(id: String = "", name: String = "智能柜", location: String = "未知") =
        deviceDao.updateDeviceInfo(Device(primaryKey = 1, name = name, id = id, location = location))
}