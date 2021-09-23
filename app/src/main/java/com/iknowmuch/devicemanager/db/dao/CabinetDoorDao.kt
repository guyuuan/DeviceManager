package com.iknowmuch.devicemanager.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.iknowmuch.devicemanager.bean.CabinetDoor
import kotlinx.coroutines.flow.Flow

/**
 *@author: Chen
 *@createTime: 2021/9/23 9:31
 *@description:
 **/
@Dao
interface CabinetDoorDao {
    @Insert
    suspend fun insertCabinetDoors(data: List<CabinetDoor>)

    @Query("SELECT * FROM cabinet_door")
    fun getCabinetDoors(): Flow<CabinetDoor>

    @Update
    suspend fun updateCabinetDoor(door: CabinetDoor)

    @Delete
    suspend fun deleteCabinetDoor(door: CabinetDoor)
}