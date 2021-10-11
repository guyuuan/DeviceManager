package com.iknowmuch.devicemanager.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
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
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCabinetDoors(data: List<CabinetDoor>)

    @Query("SELECT * FROM cabinet_door")
    fun getCabinetDoorFlow(): Flow<List<CabinetDoor>>

    @Query("SELECT * FROM cabinet_door")
    fun getCabinetDoors(): List<CabinetDoor>

    @Query("SELECT * FROM cabinet_door WHERE id == :id")
    suspend fun getCabinetDoorById(id: Int): CabinetDoor?

    @Update
    suspend fun updateCabinetDoor(door: CabinetDoor)

    @Delete
    suspend fun deleteCabinetDoor(door: CabinetDoor)
}