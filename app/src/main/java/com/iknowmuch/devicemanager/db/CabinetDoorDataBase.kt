package com.iknowmuch.devicemanager.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.iknowmuch.devicemanager.bean.CabinetDoor
import com.iknowmuch.devicemanager.db.dao.CabinetDoorDao

/**
 *@author: Chen
 *@createTime: 2021/9/23 9:35
 *@description:
 **/
@Database(entities = [CabinetDoor::class], version = 1, exportSchema = true)
abstract class CabinetDoorDataBase : RoomDatabase() {
    abstract fun getCabinetDoorDao(): CabinetDoorDao
}