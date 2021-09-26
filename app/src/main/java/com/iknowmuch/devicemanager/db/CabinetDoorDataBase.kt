package com.iknowmuch.devicemanager.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RenameColumn
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import com.iknowmuch.devicemanager.bean.CabinetDoor
import com.iknowmuch.devicemanager.db.dao.CabinetDoorDao

/**
 *@author: Chen
 *@createTime: 2021/9/23 9:35
 *@description:
 **/
@Database(
    entities = [CabinetDoor::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)]
)
abstract class CabinetDoorDataBase : RoomDatabase() {
    abstract fun getCabinetDoorDao(): CabinetDoorDao

}