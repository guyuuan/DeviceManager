package com.iknowmuch.devicemanager.bean

/**
 *@author: Chen
 *@createTime: 2021/8/31 15:15
 *@description:
 **/

abstract class BaseJson {
    abstract val status: Int
    abstract val msg: String?
}
/*
*     @Json(name = "code")
    val _code: Int,
    @Json(name = "message")
    val _message: String,
    @Json(name = "msg")
    val _msg: String,
* */