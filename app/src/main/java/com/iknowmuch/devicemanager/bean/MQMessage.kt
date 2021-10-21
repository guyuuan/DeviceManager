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
    val code: String,
    val message: String,
    val userToken: String?,
    val data: Data,
    val time: Long?
) {
    /*
    * probeCode ：探头编码
      cabinet Code：智能柜编码
      state：操作状态（0是借，1是还）
      doorNo：智能柜柜门号
      deptId：院区id
    * */
    @JsonClass(generateAdapter = true)
    data class Data(
        val probeCode: String?,
        val cabinetCode: String?,
        val state: Int?,
        val doorNo: Int?,
        val deptId: Int?,
        val newAppVersion: String?,
        val url: String?,
        val nowTime: String?
    )
}
