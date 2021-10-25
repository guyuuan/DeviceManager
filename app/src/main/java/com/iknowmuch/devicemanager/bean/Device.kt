package com.iknowmuch.devicemanager.bean

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *@author: Chen
 *@createTime: 2021/10/11 9:22
 *@description:
 **/
@Entity(tableName = "device")
data class Device(
    @PrimaryKey(autoGenerate = false)
    val primaryKey: Int = 1,
    val id: String = "",
    val name: String = "智能柜",
    val location: String = "未知",
    val enabled: Boolean = false
)
