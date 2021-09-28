package com.iknowmuch.devicemanager.bean

import com.squareup.moshi.JsonClass

/**
 *@author: Chen
 *@createTime: 2021/9/27 9:19
 *@description:
 **/
data class MQMessage(
    val topic: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class Message(
    val code: Int,
    val message: String,
    val data: Any,
    val time: Long
)
