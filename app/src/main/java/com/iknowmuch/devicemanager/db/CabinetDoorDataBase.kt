package com.iknowmuch.devicemanager.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.iknowmuch.devicemanager.bean.CabinetDoor
import com.iknowmuch.devicemanager.bean.Device
import com.iknowmuch.devicemanager.db.dao.CabinetDoorDao
import com.iknowmuch.devicemanager.db.dao.DeviceDao

/**
 *@author: Chen
 *@createTime: 2021/9/23 9:35
 *@description:
 **/
@Database(
    entities = [CabinetDoor::class, Device::class],
    version = 1,
//    autoMigrations = [AutoMigration(from = 1, to = 2),
//        AutoMigration(from = 2, to = 3)]
)
abstract class CabinetDoorDataBase : RoomDatabase() {
    abstract fun getCabinetDoorDao(): CabinetDoorDao
    abstract fun getDeviceDao(): DeviceDao
}