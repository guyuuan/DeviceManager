package com.iknowmuch.devicemanager.bean

import androidx.annotation.IntRange
import androidx.annotation.StringRes
import com.iknowmuch.devicemanager.R

/**
 *@author: Chen
 *@createTime: 2021/9/17 13:11
 *@description:
 **/
data class CabinetDoor(
    val id: Int,
    val status: Status,
    val deviceCode: String?,
    val availableTime: Float?,
    @IntRange(from = 0, to = 100) val devicePower: Int
) {
    sealed class Status(@StringRes id: Int) {
        object Empty : Status(R.string.text_status_empty)
        object Changing : Status(R.string.text_status_changing)
        object Idle : Status(R.string.text_status_idle)
        object Booked : Status(R.string.text_status_booked)
        object Fault : Status(R.string.text_status_fault)
        object Error : Status(R.string.text_status_error)
    }
}

