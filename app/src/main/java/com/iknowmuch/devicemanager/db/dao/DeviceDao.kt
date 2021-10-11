package com.iknowmuch.devicemanager.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.iknowmuch.devicemanager.bean.Device
import kotlinx.coroutines.flow.Flow

/**
 *@author: Chen
 *@createTime: 2021/10/11 9:25
 *@description:
 **/
@Dao
interface DeviceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeviceInfo(device: Device)

    @Query("SELECT * FROM device WHERE primaryKey = 1")
    fun getDeviceInfo(): Flow<Device>

    @Update
    suspend fun updateDeviceInfo(device: Device)
}