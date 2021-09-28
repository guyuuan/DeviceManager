package com.iknowmuch.devicemanager.bean

import androidx.annotation.IntRange
import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.iknowmuch.devicemanager.R

/**
 *@author: Chen
 *@createTime: 2021/9/17 13:11
 *@description:
 **/
@Entity(tableName = "cabinet_door", indices = [Index("id", unique = true)])
@TypeConverters(CabinetDoorStatusConverter::class)
data class CabinetDoor(
    @PrimaryKey(autoGenerate = true)
    val primaryKey: Int = 0,
    val id: Int,
    val status: Status,
    val probeCode: String?,
    val availableTime: Float?,
    @IntRange(from = 0, to = 100) val devicePower: Int,
    val remainingChargingTime: Long = 0
) {
    sealed class Status(@StringRes val id: Int) {
        object Empty : Status(R.string.text_status_empty)
        object Charging : Status(R.string.text_status_charging)
        object Idle : Status(R.string.text_status_idle)
        object Booked : Status(R.string.text_status_booked)
        object Fault : Status(R.string.text_status_fault)
        object Error : Status(R.string.text_status_error)
    }
}

class CabinetDoorStatusConverter {
    @TypeConverter
    fun toStatus(value: Int) = when (value) {
        0 -> CabinetDoor.Status.Empty
        1 -> CabinetDoor.Status.Charging
        2 -> CabinetDoor.Status.Idle
        3 -> CabinetDoor.Status.Booked
        4 -> CabinetDoor.Status.Fault
        else -> CabinetDoor.Status.Error
    }

    @TypeConverter
    fun toInt(status: CabinetDoor.Status) = when (status) {
        CabinetDoor.Status.Empty -> 0
        CabinetDoor.Status.Charging -> 1
        CabinetDoor.Status.Idle -> 2
        CabinetDoor.Status.Booked -> 3
        CabinetDoor.Status.Fault -> 4
        CabinetDoor.Status.Error -> 5
    }
}

